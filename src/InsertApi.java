import com.google.gson.Gson;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InsertApi {
    private static final Logger logger = Logger.getLogger(InsertApi.class.getName());
    public boolean inseridoComSucesso;

    public void inserir(DadosApi dados) {
        UUID codigoLocalizacao = UUID.randomUUID();
        String codigoLocalizacaoString = codigoLocalizacao.toString();


        Connection connection = null;

        try {
            connection = Conexao.getInstance().getConnection();

            // Verifica se o codEscritorio existe na tabela clientes
            String selectClienteSql = "SELECT * FROM apidistribuicao.clientes WHERE Cod_escritorio = ?";
            try (PreparedStatement selectClienteStatement = connection.prepareStatement(selectClienteSql)) {
                selectClienteStatement.setInt(1, dados.getCodEscritorio());
                try (ResultSet resultSetCliente = selectClienteStatement.executeQuery()) {
                    if (!resultSetCliente.next()) {

                        logger.log(Level.INFO, "Escritorio: @" + dados.getCodEscritorio() + " não existe em clientes. Inserindo...");
                        // Insere o codEscritorio na tabela clientes se não existir
                        String insertClienteSql = "INSERT INTO apidistribuicao.clientes (Cod_escritorio, Cliente_VSAP) VALUES (?, ?)";
                        try (PreparedStatement insertClienteStatement = connection.prepareStatement(insertClienteSql)) {
                            insertClienteStatement.setInt(1, dados.getCodEscritorio());
                            insertClienteStatement.setString(2, "ALTERAR CLIENTE");
                            insertClienteStatement.executeUpdate();
                        }
                        logger.log(Level.INFO, "Escritorio: " + dados.getCodEscritorio() + " inserido na tabela clientes.");
                    }
                }
            }
            // Verifica se os dados já existem no banco de dados
            String selectSql = "SELECT * FROM apidistribuicao.processo WHERE cod_processo = ?";
            try (PreparedStatement selectStatement = connection.prepareStatement(selectSql)) {
                selectStatement.setInt(1, dados.getCodProcesso()); // verifica pelo codigo do processo
                try (ResultSet resultSet = selectStatement.executeQuery()) {
                    if (!resultSet.next()) {
                        // Insere os dados no banco, caso ele não exista
                        String sql = "INSERT INTO apidistribuicao.processo(cod_processo, cod_escritorio, numero_processo, " +
                                "instancia, tribunal, sigla_sistema, comarca, orgao_julgador, tipo_processo, data_audiencia, " +
                                "data_distribuicao, valor_causa, assuntos, magistrado, cidade, uf, nome_pesquisado, data_insercao, LocatorDB) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)";

                        try (PreparedStatement statementProcesso = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                            statementProcesso.setInt(1, dados.getCodProcesso());
                            statementProcesso.setInt(2, dados.getCodEscritorio());
                            statementProcesso.setString(3, dados.getNumeroProcesso());
                            statementProcesso.setInt(4, dados.getInstancia());
                            statementProcesso.setString(5, dados.getTribunal());
                            statementProcesso.setString(6, dados.getSiglaSistema());
                            statementProcesso.setString(7, dados.getComarca());
                            statementProcesso.setString(8, dados.getOrgaoJulgador());
                            statementProcesso.setString(9, dados.getTipoDoProcesso());
                            if (dados.getDataAudiencia() != null) {
                                statementProcesso.setDate(10, new java.sql.Date(dados.getDataAudiencia().getTime()));
                            } else {
                                statementProcesso.setNull(10, 0);
                            }
                            if (dados.getDataDistribuicao() != null) {
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                                String dataDistribuicaoFormatted = dateFormat.format(dados.getDataDistribuicao());

                                try {
                                    java.util.Date parsedDate = dateFormat.parse(dataDistribuicaoFormatted);
                                    statementProcesso.setDate(11, new java.sql.Date(parsedDate.getTime()));
                                } catch (ParseException e) {
                                    logger.log(Level.WARNING, "ERRO AO INSERIR DADOS " + e);
                                }
                            } else {
                                statementProcesso.setNull(11, 0);
                            }

                            statementProcesso.setString(12, dados.getValorDaCausa());
                            // Converter a lista de assuntos em uma string JSON e retira os caracteres "[" e ""
                            List<String> assuntos = dados.getAssuntos();
                            String assuntosJson = new Gson().toJson(assuntos).replaceAll("[\\[\\]\"]", "");
                            statementProcesso.setString(13, assuntosJson);
                            statementProcesso.setString(14, dados.getMagistrado());
                            statementProcesso.setString(15, dados.getCidade());
                            statementProcesso.setString(16, dados.getUf());
                            statementProcesso.setString(17, dados.getNomePesquisado());
                            statementProcesso.setTimestamp(18, new Timestamp(System.currentTimeMillis()));
                            statementProcesso.setString(19, codigoLocalizacaoString);

                            statementProcesso.executeUpdate();

                            // Obtém o ID do processo recém-inserido
                            try (ResultSet generatedKeys = statementProcesso.getGeneratedKeys()) {
                                if (generatedKeys.next()) {
                                    int idProcesso = generatedKeys.getInt(1);

                                    // Insere os dados relacionados ao processo
                                    inserirAutores(connection, idProcesso, dados.getAutor());
                                    inserirReus(connection, idProcesso, dados.getReu());
                                    inserirAdv(connection, idProcesso, dados.getAdvogados());
                                    inserirOutrosEnvil(connection, idProcesso, dados.getOutrosEnvolvidos());
                                    inserirMovimentos(connection, idProcesso, dados.getMovimentos());
                                    inserirLink(connection, idProcesso, dados.getDocumentosIniciais());
                                    inserirDocInicial(connection, idProcesso, dados.getListaDocumentos());

                                }
                            }
                        }

                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "ERRO AO INSERIR DADOS " + e);
        } finally {
            // Fecha a conexão no bloco finally para garantir que ela seja fechada, mesmo em caso de exceção
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "ERRO AO INSERIR DADOS " + e);
            }
        }
    }

    private void inserirAutores(Connection conn, int idProcesso, List<RetornoAutor> autores) throws SQLException {
        if (autores != null && !autores.isEmpty()) {
            String sql = "INSERT INTO apidistribuicao.processo_autor (ID_processo, cod_polo, nome,descricao_polo,cnpj,cpf) VALUES (?,?,?,?,?,?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (RetornoAutor autor : autores) {
                    pstmt.setInt(1, idProcesso);
                    pstmt.setInt(2, autor.getCodPolo());
                    pstmt.setString(3, autor.getNome());
                    pstmt.setString(4, autor.getDescricaoTipoPolo());
                    pstmt.setString(5, autor.getCnpj());
                    pstmt.setString(6, autor.getCpf());

                    pstmt.executeUpdate();
                }
            }
        }
    }

    private void inserirReus(Connection conn, int idProcesso, List<RetornoReu> Reus) throws SQLException {
        if (Reus != null && !Reus.isEmpty()) {
            String sql = "INSERT INTO apidistribuicao.processo_reu (ID_processo, cod_polo, nome,descricao_polo,cnpj,cpf) VALUES (?,?,?,?,?,?)";
            try (PreparedStatement statementReu = conn.prepareStatement(sql)) {
                for (RetornoReu Reu : Reus) {
                    statementReu.setInt(1, idProcesso);
                    statementReu.setInt(2, Reu.getCodPolo());
                    statementReu.setString(3, Reu.getNome());
                    statementReu.setString(4, Reu.getDescricaoTipoPolo());
                    statementReu.setString(5, Reu.getCnpj());
                    statementReu.setString(6, Reu.getCpf());

                    statementReu.executeUpdate();

                }
            }
        }
    }

    private void inserirAdv(Connection conn, int idProcesso, List<RetornoAdvogado> Advogados) throws SQLException {
        if (Advogados != null && !Advogados.isEmpty()) {
            String sql = "INSERT INTO apidistribuicao.processo_advogado (ID_processo, cod_polo, nome,Tipo_polo,oab,cnpj,cpf) VALUES (?,?,?,?,?,?,?)";
            try (PreparedStatement statementAdv = conn.prepareStatement(sql)) {
                for (RetornoAdvogado adv : Advogados) {
                    statementAdv.setInt(1, idProcesso);
                    statementAdv.setInt(2, adv.getCodPolo());
                    statementAdv.setString(3, adv.getNome());
                    statementAdv.setString(4, adv.getTipoPolo());
                    statementAdv.setString(5, adv.getOab());
                    statementAdv.setString(6, adv.getCnpj());
                    statementAdv.setString(7, adv.getCpf());

                    statementAdv.executeUpdate();

                }
            }
        }
    }

    private void inserirOutrosEnvil(Connection conn, int idProcesso, List<RetornoOutrosEnvil> OutroEnvil) throws SQLException {
        if (OutroEnvil != null && !OutroEnvil.isEmpty()) {
            String sql = "INSERT INTO apidistribuicao.processo_outrosenvil (ID_processo, cod_polo, nome,descricao_polo,cnpj,cpf) VALUES (?,?,?,?,?,?)";
            try (PreparedStatement statementOutrosEnvil = conn.prepareStatement(sql)) {
                for (RetornoOutrosEnvil outros : OutroEnvil) {
                    statementOutrosEnvil.setInt(1, idProcesso);
                    statementOutrosEnvil.setInt(2, outros.getCodPolo());
                    statementOutrosEnvil.setString(3, outros.getNome());
                    statementOutrosEnvil.setString(4, outros.getDescricaoTipoPolo());
                    statementOutrosEnvil.setString(5, outros.getCnpj());
                    statementOutrosEnvil.setString(6, outros.getCpf());

                    statementOutrosEnvil.executeUpdate();

                }
            }
        }
    }

    private void inserirMovimentos(Connection conn, int idProcesso, List<RetornoMovimento> Movimentos) throws SQLException {
        if (Movimentos != null && !Movimentos.isEmpty()) {
            String sql = "INSERT INTO apidistribuicao.processo_movimento (ID_processo, texto,data) VALUES (?,?,?)";
            try (PreparedStatement statementMovimentos = conn.prepareStatement(sql)) {
                for (RetornoMovimento movimento : Movimentos) {
                    statementMovimentos.setInt(1, idProcesso);
                    statementMovimentos.setString(2, movimento.getTexto());
                    if (movimento.getData() != null) {
                        statementMovimentos.setDate(3, new java.sql.Date(movimento.getData().getTime()));
                    } else {
                        statementMovimentos.setNull(3, 0);
                    }
                    statementMovimentos.executeUpdate();

                }
            }
        }
    }

    private void inserirLink(Connection conn, int idProcesso, List<RetornoDocIniciais> Link) throws SQLException {
        if (Link != null && !Link.isEmpty()) {
            String sql = "INSERT INTO apidistribuicao.processo_link (ID_processo, link) VALUES (?,?)";
            try (PreparedStatement statementLink = conn.prepareStatement(sql)) {
                for (RetornoDocIniciais link : Link) {
                    statementLink.setInt(1, idProcesso);
                    statementLink.setString(2, link.getLinkDocInicial());

                    statementLink.executeUpdate();

                }
            }

        }
    }

    private void inserirDocInicial(Connection conn, int idProcesso, List<RetornoListDocument> docInicial) throws SQLException {
        if (docInicial != null && !docInicial.isEmpty()) {
<<<<<<< Updated upstream
            String sql = "INSERT INTO apidistribuicao.processo_docinicial (ID_processo, link_documento, doc_peticao_inicial) VALUES (?, ?, ?)";
            try (PreparedStatement statementDocInicial = conn.prepareStatement(sql)) {
                for (RetornoListDocument DocInicial : docInicial) {
                        statementDocInicial.setInt(1, idProcesso);
                        statementDocInicial.setString(2, DocInicial.getLinkDocumento());
                        statementDocInicial.setBoolean(3, DocInicial.isDocPeticaoInicial());
=======
            String sql = "INSERT INTO apidistribuicao.processo_docinicial (ID_processo, link_documento, tipo, doc_peticao_inicial) VALUES (?, ?, ?, ?)";
            try (PreparedStatement statementDocInicial = conn.prepareStatement(sql)) {
                for (RetornoListDocument DocInicial : docInicial) {
                    statementDocInicial.setInt(1, idProcesso);
                    statementDocInicial.setString(2, DocInicial.getLinkDocumento());

                    // Define o tipo baseado no link
                    if (DocInicial.getLinkDocumento().contains("RESUMO")) {
                        statementDocInicial.setString(3, "Resumo");
                    } else if (DocInicial.getLinkDocumento().contains("PREDITIVO")) {
                        statementDocInicial.setString(3, "Preditivo");
                    } else {
                        statementDocInicial.setString(3, "Principal");
                    }
>>>>>>> Stashed changes

                    statementDocInicial.setBoolean(4, DocInicial.isDocPeticaoInicial());

                    statementDocInicial.executeUpdate();
                    inseridoComSucesso = true;
                }
            }
        }
    }
}

