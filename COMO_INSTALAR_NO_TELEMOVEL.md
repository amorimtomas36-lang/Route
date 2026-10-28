# Guia Oficial de Instalação e Teste do RouteBoost no Telemóvel Android

Este guia foi elaborado para quem está a utilizar **exclusivamente um telemóvel Android**.

---

## 1. Onde Descarregar o APK Compilado (24 MB)

O APK já foi gerado e compilado com sucesso pelo Gradle na cloud do Google AI Studio (`app/build/outputs/apk/debug/app-debug.apk`).

### Como descarregar diretamente pelo telemóvel:
1. No cabeçalho superior do **Google AI Studio** (no navegador do seu telemóvel):
   - Toque no menu de opções (ícone de engrenagem ⚙️ ou os três pontos `⋮` no topo direito).
   - Selecione a opção **"Export"** ou **"Download Project as ZIP"** / **"Generate APK"**.
   - Se exportar o repositório ou abrir o gestor de ficheiros do AI Studio, o ficheiro binário gerado encontra-se em:
     `app/build/outputs/apk/debug/app-debug.apk`
2. Em alternativa, se tiver o projeto associado ao **GitHub**, pode criar uma **Release** ou um **GitHub Action** gratuito (que gera o APK automaticamente a cada commit com download direto no telemóvel).

---

## 2. Passos para Instalar no Android

1. Ao terminar o download do ficheiro `.apk` no telemóvel, abra os **Downloads** ou o seu gestor de ficheiros (ex: *Ficheiros*, *Files by Google* ou *Samsung Meus Ficheiros*).
2. Toque no ficheiro `app-debug.apk` (ou `RouteBoost.apk`).
3. Se o Android apresentar a mensagem *"Por motivos de segurança, o telemóvel não tem permissão para instalar aplicações desconhecidas desta fonte"*:
   - Toque em **Definições**.
   - Ative a opção **"Permitir desta fonte"** (para o navegador ou gestor de ficheiros).
   - Volte atrás e toque em **Instalar**.
4. Toque em **Abrir**.

---

## 3. Passo a Passo para Testar as Funções Reais

### Teste 1: Auditoria Real de Latência e Jitter (Sem VPN ligada)
1. Abra o RouteBoost.
2. Toque na aba **Auditoria** (ícone de gráfico na barra inferior).
3. Escolha o jogo desejado (ex: *Free Fire*, *LoL: Wild Rift*, *PUBG Mobile*).
4. Toque em **Auditar**.
5. O RouteBoost enviará 5 sondagens reais por socket TCP/UDP aos servidores oficiais do jogo:
   - Verá o **Ping Mínimo**, **Ping Médio**, **Ping Máximo (Pico)** e o **Jitter real (norma RFC 3550)** da sua operadora (4G, 5G ou Wi-Fi).

### Teste 2: Ativação da Interface Virtual TUN (VpnService)
1. Volte à aba **Início**.
2. Toque no grande botão circular **OTIMIZAR ROTA**.
3. O Android exibirá o diálogo do sistema:
   > *"RouteBoost pretende configurar uma ligação VPN que lhe permite monitorizar o tráfego de rede..."*
4. Toque em **OK / Permitir**.
5. O botão passará a **PARAR BOOST** com brilho ciano, o contador de tempo começará a contar e a notificação persistente aparecerá na barra de notificações.

### Teste 3: Roteamento Real e Split-Tunneling (Modo Jogo Único)
1. Com a opção **Split-Tunneling** ativa (na aba *Ajustes*):
   - Apenas o jogo selecionado tem os seus pacotes IP desviados pelo túnel.
   - Abra o YouTube ou o WhatsApp: a sua navegação normal continua sem qualquer lentidão.
2. Abra o jogo online:
   - Os pacotes UDP de baixa latência são capturados pela interface TUN e encaminhados pelo forwarder protegido (`UdpTunForwarder`), recebendo as respostas do servidor sem bloqueios.
   - No RouteBoost, observe o contador de **Volume Transferido (KB)** e **Pacotes Processados** a subir em tempo real!

### Teste 4: Adicionar o seu Próprio Servidor Dedicado / VPS Grátis
1. Consulte o ficheiro `/server/SETUP_SERVIDOR_GRATIS.md` para instruções de como ativar a sua VPS no **Oracle Cloud Always Free**.
2. Na aba **Servidores**, toque em **'+'**, insira o IP público da sua VPS e toque em **Salvar**.
3. O nó aparecerá na lista e poderá testar o ping real para o seu servidor a qualquer instante.
