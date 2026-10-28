#!/usr/bin/env python3
"""
RouteBoost UDP Gaming Relay & Route Metric Node.
Recebe datagramas de jogos online do telemóvel Android,
encaminha para o servidor de destino do jogo com conexões otimizadas
e fornece health-check HTTP para medição real de latência.
"""

import asyncio
import socket
import json
import struct
from http.server import HTTPServer, BaseHTTPRequestHandler
import threading

HTTP_PORT = 8080
UDP_LISTEN_PORT = 7000

class HealthHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        if self.path in ['/health', '/status', '/']:
            self.send_response(200)
            self.send_header('Content-Type', 'application/json')
            self.end_headers()
            response = {
                "service": "RouteBoost Gaming Node",
                "status": "online",
                "version": "1.0",
                "node_region": "Sao Paulo / Brazil (Equinix Hub)",
                "protocols": ["wireguard", "udp_relay"],
                "active_relay_port": UDP_LISTEN_PORT
            }
            self.wfile.write(json.dumps(response).encode('utf-8'))
        else:
            self.send_response(404)
            self.end_headers()

    def log_message(self, format, *args):
        pass

def run_http_server():
    server = HTTPServer(('0.0.0.0', HTTP_PORT), HealthHandler)
    print(f"[RouteBoost Node] Servidor HTTP de Status ativo na porta {HTTP_PORT}")
    server.serve_forever()

class UdpRelayProtocol(asyncio.DatagramProtocol):
    def __init__(self):
        self.transport = None
        # Mapeamento de clientes e conexões remotas: (client_addr, target_ip, target_port) -> socket
        self.client_routes = {}

    def connection_made(self, transport):
        self.transport = transport
        print(f"[RouteBoost Node] Relay UDP iniciado na porta {UDP_LISTEN_PORT}")

    def datagram_received(self, data, addr):
        # 1. Sondagem de baixa latência para teste de Ping e Jitter
        if len(data) >= 4 and data[:4] == b"PING":
            self.transport.sendto(b"PONG" + data[4:], addr)
            return

        # 2. Protocolo de Encaminhamento de Jogos:
        # Formato: [0x01 (versão)][0x02 (comando RELAY)][4 bytes IP Alvo][2 bytes Porta Alvo][Payload do Jogo]
        if len(data) >= 8 and data[0] == 0x01 and data[1] == 0x02:
            target_ip = socket.inet_ntoa(data[2:6])
            target_port = struct.unpack('!H', data[6:8])[0]
            payload = data[8:]

            # Encaminha o datagrama para o servidor oficial do jogo
            loop = asyncio.get_event_loop()
            loop.create_task(self.forward_to_game(addr, target_ip, target_port, payload))

    async def forward_to_game(self, client_addr, target_ip, target_port, payload):
        try:
            # Cria socket UDP de saída para o servidor do jogo
            sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
            sock.setblocking(False)
            sock.sendto(payload, (target_ip, target_port))

            # Aguarda resposta do servidor do jogo com timeout curto de baixa latência
            loop = asyncio.get_running_loop()
            data = await asyncio.wait_for(loop.sock_recv(sock, 32767), timeout=2.0)

            # Empacota a resposta e envia de volta para o telemóvel Android
            # [0x01][0x03 (RESPONSE)][4 bytes IP Origem][2 bytes Porta Origem][Payload]
            target_ip_bytes = socket.inet_aton(target_ip)
            target_port_bytes = struct.pack('!H', target_port)
            response_packet = b"\x01\x03" + target_ip_bytes + target_port_bytes + data

            self.transport.sendto(response_packet, client_addr)
            sock.close()
        except asyncio.TimeoutError:
            pass
        except Exception as e:
            pass

async def main():
    t = threading.Thread(target=run_http_server, daemon=True)
    t.start()

    loop = asyncio.get_running_loop()
    transport, protocol = await loop.create_datagram_endpoint(
        lambda: UdpRelayProtocol(),
        local_addr=('0.0.0.0', UDP_LISTEN_PORT)
    )

    try:
        await asyncio.Event().wait()
    finally:
        transport.close()

if __name__ == '__main__':
    asyncio.run(main())
