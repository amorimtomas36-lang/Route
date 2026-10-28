#!/usr/bin/env python3
"""RouteBoost UDP relay.

This relay is an experimental UDP forwarder, not a WireGuard server. Expose UDP
port 7000 only on a trusted test VPS and configure the Android client to use that
exact host/port. Do not advertise it as a production gaming VPN.
"""
import asyncio
import json
import socket
import struct
import threading
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

HTTP_PORT = 8080
UDP_LISTEN_PORT = 7000
MAX_DATAGRAM = 32767
RELAY_TIMEOUT_SECONDS = 2.0


class HealthHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        if self.path not in ("/health", "/status", "/"):
            self.send_error(404)
            return
        body = json.dumps({
            "service": "RouteBoost UDP relay",
            "status": "online",
            "version": "1.1",
            "protocol": "experimental-udp-relay",
            "udp_port": UDP_LISTEN_PORT,
            "wireguard": False,
        }).encode("utf-8")
        self.send_response(200)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def log_message(self, fmt, *args):
        pass


def run_http_server():
    ThreadingHTTPServer(("0.0.0.0", HTTP_PORT), HealthHandler).serve_forever()


class UdpRelayProtocol(asyncio.DatagramProtocol):
    def connection_made(self, transport):
        self.transport = transport
        print(f"[RouteBoost] UDP relay listening on :{UDP_LISTEN_PORT}")

    def datagram_received(self, data, addr):
        if data.startswith(b"PING"):
            self.transport.sendto(b"PONG" + data[4:], addr)
            return
        if len(data) < 8 or data[0:2] != b"\x01\x02":
            return
        target_ip = socket.inet_ntoa(data[2:6])
        target_port = struct.unpack("!H", data[6:8])[0]
        payload = data[8:]
        if not payload or target_port == 0:
            return
        asyncio.get_running_loop().create_task(
            self.forward_to_game(addr, target_ip, target_port, payload)
        )

    async def forward_to_game(self, client_addr, target_ip, target_port, payload):
        sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        sock.setblocking(False)
        try:
            # Connect the UDP socket so sock_recv() receives replies from this target.
            sock.connect((target_ip, target_port))
            loop = asyncio.get_running_loop()
            await loop.sock_sendall(sock, payload)
            data = await asyncio.wait_for(
                loop.sock_recv(sock, MAX_DATAGRAM), timeout=RELAY_TIMEOUT_SECONDS
            )
            response = (
                b"\x01\x03"
                + socket.inet_aton(target_ip)
                + struct.pack("!H", target_port)
                + data
            )
            self.transport.sendto(response, client_addr)
        except (asyncio.TimeoutError, OSError, ValueError):
            # A timeout or unreachable game endpoint is not a successful relay.
            pass
        finally:
            sock.close()


async def main():
    threading.Thread(target=run_http_server, daemon=True).start()
    loop = asyncio.get_running_loop()
    transport, _protocol = await loop.create_datagram_endpoint(
        UdpRelayProtocol, local_addr=("0.0.0.0", UDP_LISTEN_PORT)
    )
    try:
        await asyncio.Event().wait()
    finally:
        transport.close()


if __name__ == "__main__":
    asyncio.run(main())
