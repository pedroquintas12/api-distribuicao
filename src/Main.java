import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    private static String lastLogDate = null;

    static Connection conn;

    public static void main(String[] args) {


        try {
            String logDirectory = "C:\\Users\\pedro\\OneDrive - LIG CONTATO DIÁRIO FORENSE\\DISTRIBUIÇÃO\\LOG DISTRIBUICÃO\\";

            Path logDirectoryPath = Paths.get(logDirectory);
            if (!Files.exists(logDirectoryPath)) {
                Files.createDirectories(logDirectoryPath);
            }
            conn = Conexao.getInstance().getConnection();

            String currentLogDate = dataparalog();
            if (!currentLogDate.equals(lastLogDate)) {
                lastLogDate = currentLogDate;
                FileHandler fileHandler = new FileHandler(logDirectory + "log " + currentLogDate + ".txt");
                fileHandler.setFormatter(new SimpleFormatter());
                logger.addHandler(fileHandler);
            }
        } catch (Exception e) {
            logger.log (Level.WARNING, "ERRO:" + e);
        }
        Timer timer = new Timer();

        // Agenda a execução do código para ser repetida a cada 60 minutos
        timer.scheduleAtFixedRate(new AgendadorTarefa(logger), 0, 60 * 60 * 1000);
    }

    public static void executarCodigo() {
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM companies");
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String urlConsulmo = rs.getString("solucionare_distribuicao_url");
                String nomeRelacional = rs.getString("solucionare_distribuicao_nome_relacional");
                String token = rs.getString("solucionare_distribuicao_token");

                String RequestBody = "{\"nomeRelacional\":\"" + nomeRelacional + "\",\"token\":\"" + token + "\"}";
                var client = HttpClient.newHttpClient();

                // Faz o request para a API
                HttpRequest request = HttpRequest.newBuilder()
                        .POST(HttpRequest.BodyPublishers.ofString(RequestBody))
                        .header("Content-Type", "application/json")
                        .uri(URI.create(urlConsulmo))
                        .build();

                var response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenApply(HttpResponse -> {
                            int statusCode = HttpResponse.statusCode();
                            if (statusCode != 200) {
                                logger.log(Level.WARNING, "Resposta da API: " + HttpResponse.body() +"\n"+ " Status: " + HttpResponse.statusCode());
                                return null;
                            } else {
                                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                                Gson gson = new GsonBuilder()
                                        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                                        .create();
                                String jsonResponse = HttpResponse.body();
                                DadosApi[] dados = gson.fromJson(jsonResponse, DadosApi[].class);


                                return dados;
                            }
                        })
                        .thenAccept(dados -> {
                            if (dados != null) {
                                ArrayList<Object> Codigo = new ArrayList<>();
                                InsertApi inserter = new InsertApi();

                                logger.log (Level.INFO, "Inserindo Dados no banco...");
                                for (DadosApi dado : dados) {
                                    int VSAP = dado.getCodEscritorio();
                                    inserter.inserir(dado);
                                    Codigo.add(VSAP);

                                }

                                if (inserter.inseridoComSucesso) {
                                    logger.log(Level.INFO, "DADOS INSERIDOS COM SUCESSO \n" + "VSAP: " + Codigo);
                                    confirmarDados(dados);
                                }
                            }

                        })
                        .exceptionally(ex -> {
                            if (ex.getCause() instanceof RuntimeException runtimeException) {
                                logger.log(Level.SEVERE, runtimeException.getMessage());
                            } else {
                                logger.log(Level.SEVERE, "API vazia ou com erro de request", ex);
                            }
                            return null;
                        })
                        .join();
            } else {
                logger.log(Level.WARNING, "Nenhuma linha retornada da consulta.");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erro ao executar o código" + e);
        }
    }


    private static void confirmarDados(DadosApi[] dados) {
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM companies");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String urlConfirmados = rs.getString("solucionare_distribuicao_confirmardados_url");
                String nomeRelacional = rs.getString("solucionare_distribuicao_nome_relacional");
                String token = rs.getString("solucionare_distribuicao_token");

                List<Confirmardados> distribuicoes = new ArrayList<>();
                for (DadosApi dado : dados) {
                    distribuicoes.add(new Confirmardados(dado.getCodEscritorio(), dado.getCodProcesso()));
                }

                // JSON para enviar na requisição
                String requestBody = "{\"nomeRelacional\":\"" + nomeRelacional + "\",\"token\":\"" + token + "\",\"distribuicoes\":[";
                for (Confirmardados dist : distribuicoes) {
                    requestBody += "{\"codEscritorio\":" + dist.getCodEscritorio() + ",\"codProcesso\":" + dist.getCodProcesso() + "},";
                }
                requestBody = requestBody.substring(0, requestBody.length() - 1);
                requestBody += "]}";

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(urlConfirmados))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                logger.log(Level.SEVERE,"Status Code: " + response.statusCode()+"\n"+"Response Body: " + response.body());
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erro ao confirmar os dados", e);
        }
    }

    private static String obterHoraAtualFormatada() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        return dateFormat.format(cal.getTime());
    }

    private static String dataparalog() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        return dateFormat.format(cal.getTime());
    }
}



class AgendadorTarefa extends TimerTask {
    private final Logger logger;

    public AgendadorTarefa(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void run() {
        Main.executarCodigo();
    }
}
