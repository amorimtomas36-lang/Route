# Estado real da infraestrutura RouteBoost

Este documento descreve o estado atual do protótipo; não promete uma VPN pronta.

## Limitações atuais

- O encaminhador Android só trata alguns pacotes IPv4 UDP e responde a pedidos ICMP localmente.
- TCP, IPv6 e fragmentos IPv4 não são encaminhados.
- O relay UDP personalizado não é WireGuard e não fornece, por si só, um túnel VPN completo.
- A porta UDP experimental do relay é 7000; a verificação HTTP de saúde usa 8080.
- A aplicação não tem nós públicos reais pré-configurados. É necessário um servidor próprio acessível, e a implementação atual ainda não suporta todos os protocolos necessários para jogos.
- Medir o ping até ao nó não prova que a rota completa até ao servidor do jogo seja mais rápida.

## Segurança

Não execute scripts de instalação remotos não verificados com `curl | bash`. Não exponha um relay UDP sem autenticação, limites de tráfego e regras de firewall adequadas. Não introduza chaves privadas ou credenciais no repositório.

## Como validar uma futura versão

1. Compilar o APK e confirmar que o artefacto é gerado.
2. Instalar num dispositivo de teste e verificar início, paragem e recuperação da VPN.
3. Usar um servidor de teste controlado, com autenticação e firewall.
4. Testar TCP, UDP, DNS, IPv4, IPv6, fragmentação, perda de pacotes e reconexão.
5. Só declarar compatibilidade com um jogo depois de testar esse jogo e comparar latência ponta a ponta com e sem túnel.

**Estado: experimental. Não utilizar como VPN geral nem afirmar que melhora o ping de jogos até os testes acima passarem.**
