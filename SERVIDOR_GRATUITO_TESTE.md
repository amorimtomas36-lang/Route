# RouteBoost — servidor gratuito para testes

Estado em 28-10-2026.

## Resumo honesto

O APK atual pode testar a ligação direta e medir latência, mas **a VPN de otimização ainda não deve ser ativada**: falta integrar um túnel completo compatível com TCP, UDP, IPv4 e IPv6 e ligá-lo a um servidor real. Não introduzas endereços aleatórios como se fossem servidores RouteBoost.

## Opção de teste sem mensalidade: Oracle Cloud Always Free

Documentação oficial: https://docs.oracle.com/en-us/iaas/Content/FreeTier/freetier_topic-Always_Free_Resources.htm

A oferta inclui máquinas virtuais elegíveis sem custo recorrente dentro dos limites Always Free. A disponibilidade depende da região e a Oracle pode recuperar instâncias consideradas inativas. A criação de conta normalmente exige verificação por cartão e telemóvel; não é uma opção sem verificação. Se o utilizador for menor, a criação e gestão da conta deve ser feita por um responsável adulto, de acordo com os termos do fornecedor.

**Importante:** criar uma VM não cria automaticamente um servidor SOCKS5 nem faz o RouteBoost funcionar. Ainda é necessário instalar e proteger um serviço de túnel, configurar as regras de rede e integrar no Android um cliente compatível. Não abrir um proxy sem autenticação à Internet.

## Porque não usar um proxy público aleatório

- Muitos proxies SOCKS5 públicos não suportam UDP ASSOCIATE, necessário para vários jogos.
- Endereços públicos mudam ou deixam de funcionar; o tráfego pode passar por operadores desconhecidos.
- Latência até ao proxy não prova que o ping do jogo diminua.
- Nunca enviar palavras-passe, contas de jogo ou outros dados privados através de um proxy desconhecido.

## Critérios para declarar o RouteBoost funcional

1. O cliente Android encaminha TCP, UDP, IPv4 e IPv6 sem bloquear tráfego suportado.
2. O servidor tem autenticação e portas/firewall configurados de forma segura.
3. Um teste confirma que os pacotes passam pelo túnel e regressam.
4. Medir o jogo/servidor de destino antes e depois, repetindo os testes na mesma rede.
5. Só apresentar “ping melhorado” quando as medições do destino real confirmarem a melhoria.

## Próximo trabalho de engenharia

- Integrar um motor Android de túnel compatível (por exemplo, adaptar o projeto open-source SocksTun/hev-socks5-tunnel, respeitando a licença e atribuições).
- Adicionar configuração própria para SOCKS5, sem confundir SOCKS5 com o protocolo experimental UDP Relay atual.
- Criar validação de conectividade e autenticação antes de permitir ligar a VPN.
- Compilar e testar o APK por GitHub Actions; não declarar a integração concluída apenas porque a compilação passa.
