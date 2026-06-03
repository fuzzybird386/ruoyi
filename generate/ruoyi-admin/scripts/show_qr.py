#!/usr/bin/env python3
"""Display a QR code on the MIPI panel via Linux framebuffer (/dev/fb0)."""

from __future__ import annotations

import argparse
import ctypes
import fcntl
import mmap
import struct
import sys
import time
from typing import NamedTuple

from PIL import Image

FBIOGET_VSCREENINFO = 0x4600
FBIOGET_FSCREENINFO = 0x4602
QR_ECLEVEL_M = 1
QR_MODE_8 = 2


class FramebufferInfo(NamedTuple):
    width: int
    height: int
    stride: int
    bpp: int
    red_offset: int
    green_offset: int
    blue_offset: int


class QREncoder:
    def __init__(self) -> None:
        self._lib = ctypes.CDLL("libqrencode.so.4")
        self._lib.QRcode_encodeString.restype = ctypes.c_void_p
        self._lib.QRcode_encodeString.argtypes = [
            ctypes.c_char_p,
            ctypes.c_int,
            ctypes.c_int,
            ctypes.c_int,
            ctypes.c_int,
        ]
        self._lib.QRcode_free.argtypes = [ctypes.c_void_p]

    def encode(self, text: str) -> Image.Image:
        raw = text.encode("utf-8")
        ptr = self._lib.QRcode_encodeString(raw, 0, QR_ECLEVEL_M, QR_MODE_8, 1)
        if not ptr:
            raise RuntimeError("failed to encode QR code")

        class QRcode(ctypes.Structure):
            _fields_ = [
                ("version", ctypes.c_int),
                ("width", ctypes.c_int),
                ("data", ctypes.POINTER(ctypes.c_ubyte)),
            ]

        qr = ctypes.cast(ptr, ctypes.POINTER(QRcode)).contents
        modules = qr.width
        pixels = []
        for y in range(modules):
            row = []
            for x in range(modules):
                value = qr.data[y * modules + x]
                row.append(0 if value & 1 else 255)
            pixels.append(row)

        self._lib.QRcode_free(ptr)
        img = Image.new("L", (modules, modules))
        img.putdata([px for row in pixels for px in row])
        return img


def read_framebuffer_info(fb_path: str) -> FramebufferInfo:
    with open(fb_path, "rb") as fb:
        var = bytearray(160)
        fix = bytearray(80)
        fcntl.ioctl(fb, FBIOGET_VSCREENINFO, var)
        fcntl.ioctl(fb, FBIOGET_FSCREENINFO, fix)

        width, height = struct.unpack_from("II", var, 0)
        bpp = struct.unpack_from("I", var, 24)[0]
        red_off = struct.unpack_from("I", var, 32)[0]
        green_off = struct.unpack_from("I", var, 44)[0]
        blue_off = struct.unpack_from("I", var, 56)[0]
        stride = struct.unpack_from("I", fix, 52)[0]
        if stride == 0:
            stride = width * (bpp // 8)
        return FramebufferInfo(width, height, stride, bpp, red_off, green_off, blue_off)


def pack_pixel(r: int, g: int, b: int, info: FramebufferInfo) -> bytes:
    if info.bpp != 32:
        raise RuntimeError(f"unsupported bpp: {info.bpp}, expected 32")

    value = 0
    value |= (r & 0xFF) << info.red_offset
    value |= (g & 0xFF) << info.green_offset
    value |= (b & 0xFF) << info.blue_offset
    return struct.pack("<I", value)


def render_qr_image(qr_img: Image.Image, screen_w: int, screen_h: int, margin_ratio: float = 0.12) -> Image.Image:
    margin = int(min(screen_w, screen_h) * margin_ratio)
    usable = min(screen_w, screen_h) - margin * 2
    scale = max(1, usable // qr_img.width)
    qr_size = qr_img.width * scale

    canvas = Image.new("RGB", (screen_w, screen_h), "white")
    qr_rgb = qr_img.convert("RGB")
    qr_scaled = qr_rgb.resize((qr_size, qr_size), Image.NEAREST)

    x = (screen_w - qr_size) // 2
    y = (screen_h - qr_size) // 2
    canvas.paste(qr_scaled, (x, y))
    return canvas


def render_blank(screen_w: int, screen_h: int) -> Image.Image:
    return Image.new("RGB", (screen_w, screen_h), "white")


def draw_to_framebuffer(image: Image.Image, fb_path: str) -> None:
    info = read_framebuffer_info(fb_path)
    if image.size != (info.width, info.height):
        image = image.resize((info.width, info.height), Image.LANCZOS)

    pixels = image.load()
    with open(fb_path, "r+b", buffering=0) as fb:
        size = info.stride * info.height
        mem = mmap.mmap(fb.fileno(), size, mmap.MAP_SHARED, mmap.PROT_WRITE)
        try:
            row_bytes = bytearray(info.stride)
            for y in range(info.height):
                offset = 0
                for x in range(info.width):
                    r, g, b = pixels[x, y][:3]
                    row_bytes[offset : offset + 4] = pack_pixel(r, g, b, info)
                    offset += 4
                mem[y * info.stride : y * info.stride + info.stride] = row_bytes
        finally:
            mem.close()


def parse_args(argv: list[str]) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Show a QR code on the MIPI framebuffer.")
    parser.add_argument("text", nargs="?", default="", help="text or URL to encode")
    parser.add_argument("--fb", default="/dev/fb0", help="framebuffer device path")
    parser.add_argument("--once", action="store_true", help="draw once and exit")
    parser.add_argument("--clear", action="store_true", help="fill screen white and exit")
    parser.add_argument("--stdin", action="store_true", help="read QR text from stdin")
    parser.add_argument(
        "--interval",
        type=float,
        default=0.0,
        help="refresh interval in seconds; 0 means keep displaying until interrupted",
    )
    return parser.parse_args(argv)


def main(argv: list[str] | None = None) -> int:
    args = parse_args(argv or sys.argv[1:])

    if args.stdin:
        args.text = sys.stdin.read()

    try:
        screen_info = read_framebuffer_info(args.fb)

        if args.clear or not args.text.strip():
            frame = render_blank(screen_info.width, screen_info.height)
            draw_to_framebuffer(frame, args.fb)
            print(f"framebuffer cleared on {args.fb}")
            return 0

        encoder = QREncoder()
        qr_img = encoder.encode(args.text)
        frame = render_qr_image(qr_img, screen_info.width, screen_info.height)
        draw_to_framebuffer(frame, args.fb)
        print(f"QR code displayed on {args.fb} ({screen_info.width}x{screen_info.height})")
        print(f"content: {args.text}")

        if args.once:
            return 0

        if args.interval > 0:
            while True:
                time.sleep(args.interval)
                draw_to_framebuffer(frame, args.fb)
        else:
            while True:
                time.sleep(3600)
    except PermissionError:
        print("permission denied: open /dev/fb0 with sudo, or add user to the video group", file=sys.stderr)
        return 1
    except KeyboardInterrupt:
        return 0
    except OSError as exc:
        print(f"display error: {exc}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
