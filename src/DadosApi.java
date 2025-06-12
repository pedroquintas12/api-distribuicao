import java.sql.Date;
import java.util.List;

public class DadosApi extends RetornoAutor {

    private int codProcesso;
    private int codEscritorio;
    private String numeroProcesso;
    private int instancia;
    private String tribunal;
    private String siglaSistema;
    private String comarca;
    private String orgaoJulgador;
    private String tipoDoProcesso;
    private Date dataAudiencia;
    private Date dataDistribuicao;
    private String valorDaCausa;
    private List<String> assuntos;
    private String magistrado;
    private List<RetornoAutor> autor;
    private List<RetornoReu> reu;
    private List<RetornoOutrosEnvil> outrosEnvolvidos;
    private List<RetornoAdvogado> advogados;
    private List<RetornoMovimento> movimentos;
    private List<RetornoDocIniciais> documentosIniciais;
    private List<RetornoListDocument> listaDocumentos;
    private String cidade;
    private String uf;
    private String nomePesquisado;

    public int getCodProcesso() {
        return codProcesso;
    }

    public void setCodProcesso(int codProcesso) {
        this.codProcesso = codProcesso;
    }

    public int getCodEscritorio() {
        return codEscritorio;
    }

    public void setCodEscritorio(int codEscritorio) {
        this.codEscritorio = codEscritorio;
    }

    public String getNumeroProcesso() {
        return numeroProcesso;
    }

    public void setNumeroProcesso(String numeroProcesso) {
        this.numeroProcesso = numeroProcesso;
    }

    public int getInstancia() {
        return instancia;
    }

    public void setInstancia(int instancia) {
        this.instancia = instancia;
    }

    public String getTribunal() {
        return tribunal;
    }

    public void setTribunal(String tribunal) {
        this.tribunal = tribunal;
    }

    public String getSiglaSistema() {
        return siglaSistema;
    }

    public void setSiglaSistema(String siglaSistema) {
        this.siglaSistema = siglaSistema;
    }

    public String getComarca() {
        return comarca;
    }

    public void setComarca(String comarca) {
        this.comarca = comarca;
    }

    public String getOrgaoJulgador() {
        return orgaoJulgador;
    }

    public void setOrgaoJulgador(String orgaoJulgador) {
        this.orgaoJulgador = orgaoJulgador;
    }

    public String getTipoDoProcesso() {
        return tipoDoProcesso;
    }

    public void setTipoDoProcesso(String tipoDoProcesso) {
        this.tipoDoProcesso = tipoDoProcesso;
    }

    public Date getDataAudiencia() {
        return dataAudiencia;
    }

    public void setDataAudiencia(Date dataAudiencia) {
        this.dataAudiencia = dataAudiencia;
    }

    public Date getDataDistribuicao() {
        return dataDistribuicao;
    }

    public void setDataDistribuicao(Date dataDistribuicao) {
        this.dataDistribuicao = dataDistribuicao;
    }

    public String getValorDaCausa() {
        return valorDaCausa;
    }

    public void setValorDaCausa(String valorDaCausa) {
        this.valorDaCausa = valorDaCausa;
    }

    public List<String> getAssuntos() {
        return assuntos;
    }

    public void setAssuntos(List<String> assuntos) {
        this.assuntos = assuntos;
    }

    public String getMagistrado() {
        return magistrado;
    }

    public void setMagistrado(String magistrado) {
        this.magistrado = magistrado;
    }

    public List<RetornoAutor> getAutor() {
        return autor;
    }

    public void setAutor(List<RetornoAutor> autor) {
        this.autor = autor;
    }

    public List<RetornoReu> getReu() {
        return reu;
    }

    public void setReu(List<RetornoReu> reu) {
        this.reu = reu;
    }

    public List<RetornoOutrosEnvil> getOutrosEnvolvidos() {
        return outrosEnvolvidos;
    }

    public void setOutrosEnvolvidos(List<RetornoOutrosEnvil> outrosEnvolvidos) {
        this.outrosEnvolvidos = outrosEnvolvidos;
    }

    public List<RetornoAdvogado> getAdvogados() {
        return advogados;
    }

    public void setAdvogados(List<RetornoAdvogado> advogados) {
        this.advogados = advogados;
    }

    public List<RetornoMovimento> getMovimentos() {
        return movimentos;
    }

    public void setMovimentos(List<RetornoMovimento> movimentos) {
        this.movimentos = movimentos;
    }

    public List<RetornoDocIniciais> getDocumentosIniciais() {
        return documentosIniciais;
    }

    public void setDocumentosIniciais(List<RetornoDocIniciais> documentosIniciais) {
        this.documentosIniciais = documentosIniciais;
    }

    public List<RetornoListDocument> getListaDocumentos() {
        return listaDocumentos;
    }

    public void setListaDocumentos(List<RetornoListDocument> listaDocumentos) {
        this.listaDocumentos = listaDocumentos;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public String getNomePesquisado() {
        return nomePesquisado;
    }

    public void setNomePesquisado(String nomePesquisado) {
        this.nomePesquisado = nomePesquisado;
    }
}
