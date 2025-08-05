
# Best Sticker App
## Overview
<div style="text-align: justify;">
Olá! Eu adoro as figurinhas do WhatsApp, sempre que termino uma frase mando uma figurinha. Decidi fazer este aplicativo porque infelizmente, porém compreensível, os aplicativos que já vi contêm propagandas. Muitas vezes eu só quero adicionar **uma** figurinha e eu preciso assistir uma propaganda de 30 segundos :/


Fiquei curioso sobre como o mundo das figurinhas poderia funcionar por de trás dos panos, procurei alguma documentação ou coisa do tipo e achei um projeto do próprio WhatsApp com um exemplo de aplicativo de figurinhas: https://github.com/WhatsApp/stickers

Utilizei o projeto do WhatsApp como base, construí a minha versão em cima do código do aplicativo de exemplo. Isso foi importante e necessário para eu aprender Android e também entender os passos necessários para passar os dados dos pacotes de figurinhas e das figurinhas em si para o WhatsApp.

**Observação**: vou alternar o nome WhatsApp com o nome "Whats".
</div>

## Componentes do projeto

O Best Sticker App usa algumas tecnologias para funcionar. Vou listá-las aqui e explicarei futuramente neste readme.

#### Aplicativo
- Java 17;
- Android;
- Gradle;
- SQLite;
- Testes: JUnit, Mockito e Robolectric;
- Dependências importantes: OkHttp, Gson e Plugin Farebase;

#### Serviços

Para o aplicativo funcionar completamente, existem alguns serviços feitos por mim que ajudam o aplicativo a funcionar. Todos eles são funções Lambda AWS que estão disponíveis no mesmo API Gateway (AWS) :
- **StickerImageConverter**: Recebe uma imagem em Base64 e converte ela para o formato WEBP (formato que o WhatsApp demanda que as figurinhas estejam);
- **StickerExceptionReceiver**: Recebe uma mensagem contendo detalhes da exception que ocorreu no aplicativo e salva em um Bucket S3 (AWS);
- **StickerLatestVersionText**: Retorna um json com as informações de uma atualização do aplicativo. Essa informações são usadas para o aplicativo mostrar as alterações ao usuário e também contém algumas informações necessárias para a atualização do aplicativo. As informações da versão ficam num Bucket S3 também;
- **StickerXAPIKeyAuthorizer**: Esta é a função que autentica as requisições enviadas ao API Gateway. Cada versão contém um token gerado no momento do build da release que é armazenado em um Bucket S3. As requisições só são válidas se conterem um token válido (mais detalhes serão mostrados em outro momento);

### Entidades
Presumivelmente, as únicas entidades que existem no projeto, por enquanto, são:
- Sticker;
- StickerPack;



## Pendências do projeto

### Pendências inadiáveis

#### ~~Atualização do aplicativo~~
Implementar uma funcionalidade de atualização do aplicativo, baixando e instalando novas versões caso o usuário aceite.

### Pendências adiáveis
#### Terminar o read.me
Terminar de escrever esse documento;
#### Editar imagem selecionada para pacote ou figurinha
<div style="text-align: justify;">
Atualmente a imagem selecionada é achatada ou espremida para caber dentro do tamanho exigido pelo WhatsApp que é de 512x512 para figurinhas e 96x96 para a imagem do pacote. Decidi não gastar tempo com isso agora porque 99% das figurinhas que eu faço ficam mais engraçadas achatadas, porém sei que outros usuários vão se incomodar com isso.
</div>

#### Funcionalidade de compartilhar pacote de figurinhas
<div style="text-align: justify;">
Acho que seria interessante esta funcionalidade. Normalmente eu compartilho as figurinhas enviando todas que eu tenho, então acho que seria algo interessante, além de ser muito bom como aprendizado.
</div>

#### Banimento por figurinha indevida
<div style="text-align: justify;">
Não aceito que meu aplicativo seja usado para criação de figurinhas que promovem discurso de ódio, então vou desenvolver uma funcionalidade que usa I.A. para identificar se a figurinha contém algum tipo de símbolo de ódio ou outras coisas ruins.
</div>

#### Mudar a mensagem de atualização de pacote
O Whats coloca uma mensagem de "tal pacote já foi adicionado".
