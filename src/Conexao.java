import java.sql.Connection;


import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Conexao {

    private static final Logger logger = Logger.getLogger(Conexao.class.getName());
    private static Conexao conexao;

    public static Conexao getInstance(){
        if(conexao == null){
            conexao = new Conexao();
        }
        return conexao;

    }

    public Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection("jdbc:mysql://26.191.28.12:3306/apidistribuicao","pedro","123456");
     }
     public static void main(String[]args){
        try {
            System.out.println(getInstance().getConnection());

        }catch (Exception e){
          logger.log(Level.CONFIG ,"erro de conexão" + e);
        }
     }


}
