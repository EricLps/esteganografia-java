package com.example.steganography;

import java.io.File;
import java.util.NoSuchElementException;
import java.util.Scanner;

import com.example.steganography.exceptions.SteganographyException;

public class MainApplication {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ImageProcessor imageProcessor = new ImageProcessor();
        DataConverter dataConverter = new DataConverter();
        SteganographyUtils steganography = new SteganographyUtils(imageProcessor, dataConverter);

        System.out.println("Bem-vindo à Aplicação de Esteganografia LSB!");
        System.out.println("Escolha uma opção:");
        System.out.println("1. Ocultar Dados (Embed)");
        System.out.println("2. Extrair Dados (Extract)");
        System.out.println("3. Sair");

        String choice;
        try {
            while (true) {
                System.out.print("Sua escolha: ");
                choice = scanner.nextLine();

                switch (choice) {
                    case "1":
                        System.out.println("\n--- MODO OCULTAR DADOS ---");
                        try {
                            System.out.print("Caminho da imagem PNG original: ");
                            String originalImagePath = scanner.nextLine();

                            System.out.print("Caminho da pasta/arquivo a ser ocultado (e.g., /caminho/para/pasta_secreta ou /caminho/para/arquivo.txt): ");
                            String dataToHidePath = scanner.nextLine();

                            System.out.print("Caminho temporário para o arquivo ZIP gerado (e.g., ./temp/hidden_data.zip): ");
                            String zipTempPath = scanner.nextLine();

                            System.out.print("Caminho para salvar a imagem com dados ocultos (e.g., ./output/imagem_oculta.png): ");
                            String outputImagePath = scanner.nextLine();

                            // garante a existencia do diretorio
                            File tempZipFile = new File(zipTempPath);
                            File tempDir = tempZipFile.getParentFile();
                            if (tempDir != null && !tempDir.exists()) {
                                tempDir.mkdirs();
                            }

                            // existencia do diretorio oculto
                            File outputImageFile = new File(outputImagePath);
                            File outputDir = outputImageFile.getParentFile();
                            if (outputDir != null && !outputDir.exists()) {
                                outputDir.mkdirs();
                            }

                            System.out.println("Iniciando ocultação de dados...");
                            steganography.embedData(originalImagePath, dataToHidePath, zipTempPath, outputImagePath);
                            System.out.println("Ocultação concluída com sucesso!");

                        } catch (NoSuchElementException e) {
                            System.err.println("ERRO DE ENTRADA: Nenhuma linha encontrada. Certifique-se de que não fechou o terminal ou apertou Ctrl+Z/Ctrl+D acidentalmente.");
                            e.printStackTrace();
                        } catch (SteganographyException e) {
                            System.err.println("Erro ao ocultar dados: " + e.getMessage());
                            if (e.getCause() != null) {
                                System.err.println("Causa: " + e.getCause().getMessage());
                            }
                        } catch (Exception e) {
                            System.err.println("ERRO INESPERADO durante o processo de ocultação:");
                            e.printStackTrace();
                        }
                        break;

                    case "2":
                        System.out.println("\n--- MODO EXTRAIR DADOS ---");
                        try {
                            System.out.print("Caminho da imagem PNG com dados ocultos: ");
                            String stegoImagePath = scanner.nextLine();

                            System.out.print("Caminho do diretório para salvar os dados extraídos (e.g., ./extracted_files): ");
                            String outputDirectory = scanner.nextLine();

                            // verifica a existencia do diretorio de saida
                            File extractDir = new File(outputDirectory);
                            if (!extractDir.exists()) {
                                extractDir.mkdirs();
                            }

                            System.out.println("Iniciando extração de dados...");
                            steganography.extractData(stegoImagePath, outputDirectory);
                            System.out.println("Extração concluída com sucesso! Verifique a pasta: " + outputDirectory);

                        } catch (NoSuchElementException e) {
                            System.err.println("ERRO DE ENTRADA: Nenhuma linha encontrada. Certifique-se de que não fechou o terminal ou apertou Ctrl+Z/Ctrl+D acidentalmente.");
                            e.printStackTrace();
                        } catch (SteganographyException e) {
                            System.err.println("Erro ao extrair dados: " + e.getMessage());
                            if (e.getCause() != null) {
                                System.err.println("Causa: " + e.getCause().getMessage());
                            }
                        } catch (Exception e) {
                            System.err.println("ERRO INESPERADO durante o processo de extração:");
                            e.printStackTrace();
                        }
                        break;
                    case "3":
                        System.out.println("Saindo da aplicação. Até logo!");
                        return;
                    default:
                        System.out.println("Opção inválida. Por favor, escolha 1, 2 ou 3.");
                        break;
                }
                System.out.println("\n-------------------------------------------------");
                System.out.println("Escolha uma opção:");
                System.out.println("1. Ocultar Dados (Embed)");
                System.out.println("2. Extrair Dados (Extract)");
                System.out.println("3. Sair");
            }
        } finally {
            scanner.close();
        }
    }
}