#!/bin/bash
# ==============================================================================
# RouteBoost - Script Automático de Instalação de Nó de Roteamento para Jogos
# Suporte: Ubuntu 20.04 / 22.04 / 24.04 LTS e Debian 11 / 12
# ==============================================================================

set -e

echo "============================================="
echo "  RouteBoost - Instalador de Nó de Roteamento"
echo "============================================="

# 1. Atualizar repositórios e instalar WireGuard e dependências
echo "[1/4] A atualizar sistema e a instalar WireGuard..."
apt-get update -y
apt-get install -y wireguard iptables qrencode curl python3

# 2. Ativar encaminhamento de pacotes IPv4 no Kernel Linux
echo "[2/4] A ativar IPv4 Forwarding..."
sysctl -w net.ipv4.ip_forward=1
echo "net.ipv4.ip_forward=1" >> /etc/sysctl.conf

# 3. Gerar chaves criptográficas WireGuard
echo "[3/4] A gerar chaves de segurança WireGuard..."
mkdir -p /etc/wireguard
cd /etc/wireguard
umask 077

wg genkey | tee server_private.key | wg pubkey > server_public.key
wg genkey | tee client_private.key | wg pubkey > client_public.key

SERVER_PRIV=$(cat server_private.key)
SERVER_PUB=$(cat server_public.key)
CLIENT_PRIV=$(cat client_private.key)
CLIENT_PUB=$(cat client_public.key)

# Descobrir interface de rede principal
NET_IF=$(ip route get 8.8.8.8 | awk '{print $5; exit}')
PUBLIC_IP=$(curl -s https://api.ipify.org || hostname -I | awk '{print $1}')

# Escrever configuração do servidor
cat <<EOF > /etc/wireguard/wg0.conf
[Interface]
Address = 10.150.0.1/24
ListenPort = 51820
PrivateKey = $SERVER_PRIV
MTU = 1420

PostUp = iptables -A FORWARD -i wg0 -j ACCEPT; iptables -t nat -A POSTROUTING -o $NET_IF -j MASQUERADE
PostDown = iptables -D FORWARD -i wg0 -j ACCEPT; iptables -t nat -D POSTROUTING -o $NET_IF -j MASQUERADE

[Peer]
PublicKey = $CLIENT_PUB
AllowedIPs = 10.150.0.2/32
EOF

# 4. Iniciar e habilitar o serviço WireGuard
echo "[4/4] A iniciar serviço WireGuard..."
systemctl enable wg-quick@wg0
systemctl restart wg-quick@wg0

echo ""
echo "=========================================================="
echo "  SUCESSO! O seu nó RouteBoost está ativo."
echo "=========================================================="
echo "IP do Servidor: $PUBLIC_IP"
echo "Porta: 51820 (UDP)"
echo ""
echo "Abra o aplicativo RouteBoost no telemóvel Android:"
echo "1. Vá a 'Servidores' > '+' (Novo Nó)"
echo "2. Insira o IP: $PUBLIC_IP"
echo "3. Porta: 51820"
echo "=========================================================="
