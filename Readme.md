# Documentação do Projeto: Esteganografia LSB em Java ☕

## 🎯 Visão Geral

Este projeto consiste em uma **aplicação em Java** desenvolvida para realizar **esteganografia digital** em imagens no formato **PNG**, utilizando a técnica **LSB (Least Significant Bit - Bit Menos Significativo)**. Sua principal funcionalidade é permitir a **ocultação de dados** (arquivos ou pastas compactadas em ZIP) dentro de uma imagem e, posteriormente, a **extração** desses mesmos dados da imagem modificada.

### O que é Esteganografia LSB? 

A esteganografia é a arte e a ciência de **escrever mensagens ocultas** de tal forma que ninguém, além do remetente e do receptor pretendidos, suspeite da existência da mensagem.

A técnica **LSB** envolve a inserção de bits dos dados a serem ocultados nos **bits menos significativos** dos pixels de uma imagem. Como esses bits são os que menos contribuem para o valor total de uma cor, sua alteração resulta em uma **mudança visualmente imperceptível** na imagem original. Isso garante que a imagem esteganografada mantenha a sua aparência.

---

## ✨ Funcionalidades Principais
O projeto oferece dois modos de operação distintos:

* **Modo Embed (Ocultar Dados):**
    * **Entrada:** Recebe uma imagem PNG de base e o caminho para a pasta/arquivos que você deseja ocultar.
    * **Compactação Automática:** Os dados fornecidos são automaticamente compactados em um arquivo ZIP.
    * Gera um cabeçalho (4 bytes) contendo o tamanho do arquivo ZIP para facilitar a recuperação.
    * **Geração de Cabeçalho:** Um cabeçalho de 4 bytes é gerado, contendo o tamanho exato do arquivo ZIP compactado. Isso é crucial para a recuperação posterior.
    * **Ocultação LSB:** O conteúdo do ZIP (incluindo o cabeçalho) é convertido em uma sequência de bits. Esses bits substituem os **bits menos significativos (LSB)** dos canais de cor (Vermelho, Verde, Azul) de cada pixel da imagem PNG.
    * **Saída:** Uma nova imagem PNG é gerada e salva, contendo os dados ocultos de forma imperceptível.

* **Modo Extract (Recuperar Dados):**
    * **Entrada:** Recebe uma imagem PNG que previamente teve dados ocultos.
    * **Leitura LSB:** A aplicação lê os bits menos significativos de cada canal RGB dos pixels da imagem.
    * **Reconstrução do Cabeçalho:** Os primeiros bits extraídos são utilizados para reconstruir o cabeçalho, permitindo determinar o tamanho dos dados ocultos.
    * **Reconstrução do ZIP:** A sequência de bytes do arquivo ZIP original é remontada.
    * **Saída:** O arquivo ZIP recuperado é salvo e descompactado automaticamente em um diretório de saída especificado, revelando os arquivos originais.
 
---

## 💡 Como Funciona (Detalhes Técnicos)

* **Formato da Imagem**: O formato **PNG (Portable Network Graphics)** é o único suportado. Ele foi escolhido por ser um formato de imagem **sem perdas (lossless)**. Essa característica é **crucial para a esteganografia LSB**, pois a compressão com perdas (como o JPEG) introduziria artefatos que danificariam ou destruiriam os dados ocultos.
* **Capacidade de Ocultação**: Cada pixel de uma imagem RGB possui 3 canais de cor (Vermelho, Verde, Azul). Ao utilizar 1 bit LSB por canal, cada pixel é capaz de carregar **3 bits de dados ocultos**.
    * **Exemplo de Capacidade**: Para uma imagem de 3MB, a capacidade de ocultação estimada é de aproximadamente **368KB**, embora o valor exato dependa das dimensões em pixels da imagem.
      
* Cabeçalho de Dados: Um **cabeçalho de 4 bytes** é estrategicamente prefixado aos dados ZIP. Este cabeçalho armazena o **tamanho exato em bytes do arquivo ZIP oculto**. Isso é fundamental para que o processo de extração saiba precisamente quantos bits precisa ler da imagem para recuperar todo o arquivo ZIP, evitando a leitura de dados irrelevantes ou incompletos.

---

## 📂 Estrutura do Projeto

A organização do projeto segue uma estrutura de pacotes lógica, visando a separação clara das responsabilidades e a manutenibilidade do código:

````
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── example/
│                   └── steganography/
│                       ├── exceptions/
│                       │   └── SteganographyException.java  # Exceção personalizada
│                       ├── DataConverter.java           # Lógica de ZIP e conversão byte&lt;->bit
│                       ├── ImageProcessor.java          # Lógica de leitura/escrita/pixel da imagem
│                       ├── MainApplication.java         # Ponto de entrada e interface CLI
│                       ├── SteganographyUtils.java      # Lógica principal de embed/extract
│                       └── ui/                          # (Opcional) Para futura interface gráfica
├── target/                 # Diretório de build do Maven (ignorados pelo Git)
├── resources/              # Recursos do projeto (imagens de teste, etc.)
│   ├── input/
│   │   └── img_teste.png   # Exemplo de imagem de entrada
│   └── output/             # Diretório para imagens de saída geradas
├── data/                   # Exemplo de diretório para dados a serem ocultados
├── temp/                   # Exemplo de diretório para arquivos ZIP temporários
├── extracted_data/         # Exemplo de diretório para dados extraídos
├── pom.xml                 # Arquivo de configuração do Maven
└── Readme.md               # Este arquivo
````

---

## 🚀 Primeiros Passos: Configuração e Execução

Para configurar e executar o projeto em sua máquina local, siga os passos abaixo:

### Pré-requisitos

Certifique-se de ter as seguintes ferramentas instaladas em seu sistema:

* **Java Development Kit (JDK) 11 ou superior** (Recomendado: JDK 21).
    * Verifique com ``java -version`` e ``javac -version``.
* **Apache Maven 3.6.0 ou superior.**
    * Verifique com ``mvn -version``.
    * Importante: Se o Maven não estiver instalado ou não estiver no seu PATH, siga as instruções de instalação para seu sistema operacional e configure as variáveis de ambiente (``M2_HOME`` e adicione ``%M2_HOME%\bin`` ao ``Path`` no Windows, ou similar no Linux/macOS).
* **Git** (para clonar o repositório).

### Clonando o Repositório

```bash
git clone https://github.com/EricLps/esteganografia-java.git
cd esteganografia
````

### Construindo o Projeto
Navegue até a raiz do projeto (onde o ``pom.xml`` está localizado) e use o Maven para construir o projeto. Certifique-se de que o seu pom.xml esteja configurado conforme o exemplo mais recente fornecido (incluindo o **maven-assembly-plugin**).

````
mvn clean install
````

Este comando irá compilar o código-fonte, executar a fase de ``package`` e gerar o arquivo JAR executável (``esteganografia.jar``) dentro do diretório ``target/``.

### Executando a Aplicação
Após a construção bem-sucedida, você pode executar a aplicação a partir do terminal:

````
java -jar target/esteganografia.jar
````
A aplicação apresentará um menu interativo no console.

**1. Ocultar Dados (Modo Embed)**

Escolha a opção 1 no menu e siga as instruções no console. Você precisará fornecer os caminhos completos para os arquivos/diretórios.

**Exemplo de Interação:**

````
Sua escolha: 1
--- MODO OCULTAR DADOS ---
Caminho da imagem PNG original: C:\caminho\para\seu\projeto\esteganografia\resources\input\minha_imagem_original.png
Caminho da pasta/arquivo a ser ocultado (e.g., /caminho/para/pasta_secreta ou /caminho/para/arquivo.txt): C:\caminho\para\seu\projeto\esteganografia\data\meus_arquivos_secretos
Caminho temporário para o arquivo ZIP gerado (e.g., ./temp/hidden_data.zip): C:\caminho\para\seu\projeto\esteganografia\temp\dados_ocultos.zip
Caminho para salvar a imagem com dados ocultos (e.g., ./output/imagem_oculta.png): C:\caminho\para\seu\projeto\esteganografia\output\minha_imagem_oculta.png
Iniciando ocultação de dados...
Imagem original carregada: C:\caminho\para\seu\projeto\esteganografia\resources\input\minha_imagem_original.png
Dados compactados em ZIP. Tamanho: XXX bytes.
Cabeçalho gerado. Tamanho: 4 bytes.
Dados (com cabeçalho) convertidos para YYY bits.
Capacidade da imagem suficiente. Bits a ocultar: YYY, Capacidade disponível: ZZZ
Dados ocultados na imagem. Bits processados: YYY
Imagem com dados ocultos salva em: C:\caminho\para\seu\projeto\esteganografia\output\minha_imagem_oculta.png
Ocultação concluída com sucesso!
````

**Observações Importantes para Ocultação:**

- **Caminhos**: Sempre utilize caminhos absolutos e completos para arquivos e pastas para evitar erros de localização.
- **Imagem Original**: Deve ser um arquivo **PNG válido**.
- **Dados a Ocultar**: Pode ser um único arquivo (ex: ``C:\caminho\para\arquivo.txt)`` ou uma pasta inteira (ex: ``C:\caminho\para\minha_pasta)``. A aplicação fará a compactação automática para ZIP.
- **Caminho Temporário do ZIP**: Forneça o caminho incluindo o nome do arquivo ZIP (ex: ``...\temp\nome_do_zip.zip)``. Certifique-se de que o diretório pai (ex: temp/) exista.
- **Caminho da Imagem de Saída**: Forneça o caminho **incluindo o nome da nova imagem PNG** (ex: ``...\output\nova_imagem.png``). Certifique-se de que o diretório pai (ex: output/) exista.


**2. Extrair Dados (Modo Extract)**

Escolha a opção ``2`` no menu e siga os prompts no console.

**Exemplo de Interação:**

````
Sua escolha: 2
--- MODO EXTRAIR DADOS ---
Caminho da imagem PNG com dados ocultos: C:\caminho\para\seu\projeto\esteganografia\output\minha_imagem_oculta.png
Caminho do diretório para salvar os dados extraídos (e.g., ./extracted_files): C:\caminho\para\seu\projeto\esteganografia\extracted_data
Iniciando extração de dados...
Imagem esteganografada carregada: C:\caminho\para\seu\projeto\esteganografia\output\minha_imagem_oculta.png
Cabeçalho (bits) extraído.
Tamanho do arquivo ZIP a ser extraído: XXX bytes.
Total de bits a extrair: YYY
Todos os YYY bits de dados (cabeçalho + ZIP) extraídos.
Dados ZIP convertidos para bytes. Tamanho: XXX bytes.
Dados descompactados com sucesso para: C:\caminho\para\seu\projeto\esteganografia\extracted_data
Extração concluída com sucesso! Verifique a pasta: C:\caminho\para\seu\projeto\esteganografia\extracted_data
````

### Observações Importantes:

- **Imagem com Dados Ocultos**: Deve ser a imagem PNG que foi gerada pelo modo "Embed".
- **Diretório de Saída**: Forneça o caminho para uma pasta onde os arquivos extraídos serão salvos (ex: ``C:\caminho\para\seu\projeto\esteganografia\extracted_data)``. A aplicação **criará a pasta se ela não existir**.

---

⚠️ Tratamento de Erros

A aplicação implementa um robusto **tratamento de erros** utilizando a **exceção personalizada** **SteganographyException*. Esta exceção é lançada para gerenciar problemas como:

- Arquivo de imagem não encontrado ou em formato inválido.
- Imagem de entrada com tamanho insuficiente para ocultar os dados desejados.
- Erros de **IO (Input/Output)** durante operações de arquivo (leitura e escrita).
- Problemas na estrutura dos dados ocultos (por exemplo, um cabeçalho inválido que impede a correta leitura do tamanho do arquivo ZIP).

**Mensagens de erro detalhadas** serão exibidas diretamente no console caso algum problema ocorra, auxiliando na depuração.

## 🤝 Como Contribuir
Contribuições para este projeto são **muito bem-vindas**! Se você encontrar bugs, tiver sugestões de melhoria ou quiser adicionar novas funcionalidades, sinta-se à vontade para:

- Abrir uma Issue no repositório para relatar bugs ou propor novas ideias.
- Abrir um Pull Request com suas contribuições de código.

## 📧 Contato
Se você tiver dúvidas ou sugestões, sinta-se à vontade para entrar em contato:

Eric Lopes
eric.lwinkelmann@gmail.com
