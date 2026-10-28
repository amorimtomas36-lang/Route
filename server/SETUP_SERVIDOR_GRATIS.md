# Guia de Infraestrutura Gratuita e Configuração de Servidores RouteBoost

Este guia foi concebido especialmente para quem está a trabalhar **diretamente a partir de um telemóvel Android**, sem necessidade de computador.

---

## 1. Por que é necessário um Servidor Intermediário? (Verdade Técnica)

Ao contrário de promessas milagrosas falsas:
- **Uma app local no telemóvel não pode encurtar fisicamente o cabo de fibra ótica** entre a sua casa e o servidor do jogo.
- O que ferramentas reais como o **ExitLag** fazem chama-se **Otimização de Rota e Peering BGP**:
  1. A sua operadora móvel (4G/5G) muitas vezes dá voltas longas e passa por nós congestionados antes de chegar ao jogo.
  2. Ao ligar a um **servidor RouteBoost intermediário** (estrategicamente posicionado em São Paulo, Miami ou Frankfurt num Data Center Tier-1 com ligação direta ao IX.br/Equinix), os seus pacotes pegam uma "autoestrada rápida".
  3. O RouteBoost mede a latência direta vs a rota intermediária. **Se a sua rota direta já for melhor, o RouteBoost não força o desvio**, poupando latência e bateria!

---

## 2. Como criar um Servidor 100% Gratuito (Pelo Telemóvel)

### Opção A: Oracle Cloud Always Free (A Melhor Opção - Grátis Para Sempre)
A Oracle oferece gratuitamente no seu plano vitalício:
- **4 núcleos Ampere ARM, 24 GB de RAM**
- **10 Terabytes de tráfego por mês** (mais que suficiente para centenas de horas de jogo)
- Data centers em: **São Paulo (Brasil)**, **Santiago (Chile)**, **Frankfurt (Alemanha)**, **Ashburn (EUA)**.

#### Passo a Passo no Telemóvel:
1. Abra o navegador do telemóvel (Chrome/Brave) e aceda a `https://www.oracle.com/cloud/free/`.
2. Registe a sua conta gratuita selecionando a região mais próxima dos seus jogos (ex: *Brazil East - São Paulo*).
3. No painel da Oracle:
   - Toque no menu hambúrguer > **Compute** > **Instances** > **Create Instance**.
   - Escolha o sistema operacional **Ubuntu 22.04 LTS**.
   - Toque em **Save Private Key** (guardará a chave SSH nos downloads do telemóvel).
4. No menu da rede da sua instância (**Virtual Cloud Network** > **Security Lists** > **Default Security List**):
   - Adicione uma **Ingress Rule**:
     - *Source CIDR*: `0.0.0.0/0`
     - *IP Protocol*: `UDP`
     - *Destination Port*: `51820` (Porta do WireGuard)
     - *Destination Port*: `8080` (Porta de Status)

---

### Opção B: Instalação com 1 Comando no Servidor

Pelo telemóvel, pode aceder ao terminal do seu servidor através da app gratuita **Termux** (disponível no F-Droid ou GitHub) ou diretamente pelo console web do navegador da Oracle Cloud:

Execute este único comando:

```bash
curl -sSL https://raw.githubusercontent.com/routeboost/server/main/install.sh | bash
```

O script instala automaticamente:
- O módulo de Kernel do WireGuard
- O encaminhamento de pacotes `sysctl net.ipv4.ip_forward=1`
- As regras de firewall `iptables` de baixa latência
- Gera as chaves de encriptação

---

## 3. Ligar o RouteBoost no Telemóvel ao seu Servidor

1. Abra o **RouteBoost** no seu telemóvel Android.
2. Toque na aba **Servidores**.
3. Toque no ícone **'+'** no canto superior direito.
4. Preencha:
   - **Nome do Nó**: Ex: *Meu VPS São Paulo*
   - **Cidade**: *São Paulo*
   - **País**: *Brasil*
   - **IP ou Domínio**: O IP público da sua VPS (ex: `129.148.x.x`)
   - **Porta**: `51820`
5. Toque em **Salvar Servidor**.
6. Pronto! O nó aparecerá com a bandeira ⚡ em *Personalizados*, com teste de ping real e pronto para otimizar os seus jogos!

---

## 4. Como Gerar o APK pelo Telemóvel

No Google AI Studio:
1. Toque no menu de configurações do projeto no canto superior direito.
2. Selecione **Export as APK** ou **Download ZIP**.
3. Se descarregar o APK diretamente, toque em **Instalar** no telemóvel (ative a permissão de "Instalar apps desconhecidas" no navegador).
4. Ao abrir a app e tocar em **OTIMIZAR ROTA**, o Android pedirá a confirmação oficial da VPN: toque em **Permitir**.
