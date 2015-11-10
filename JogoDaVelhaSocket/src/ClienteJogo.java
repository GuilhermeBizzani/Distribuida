import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Protocolo utilizado:
 *
 *  Client -> Server           Server -> Client
 *  ----------------           ----------------
 *  MOVE <n>  (0 <= n <= 8)    WELCOME <char>  (char in {X, O})
 *  QUIT                       VALID_MOVE
 *                             OTHER_PLAYER_MOVED <n>
 *                             VICTORY
 *                             DEFEAT
 *                             TIE
 *                             MESSAGE <text>
 *
 */
public class ClienteJogo {

	public static String serverIP;
	
    private JFrame frame = new JFrame("Tic Tac Toe");
    public JFrame getFrame() {
		return frame;
	}

	private JLabel messageLabel = new JLabel("");
    private ImageIcon icon;
    private ImageIcon opponentIcon;

    private Square[] board = new Square[9];
    private Square currentSquare;

    private static int PORT = 9002;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    /**
     * Construção do layout.
     */
    public ClienteJogo(String serverAddress) throws Exception {

        // Setup networking
    	serverIP = serverAddress;
        socket = new Socket(serverAddress, PORT);
        in = new BufferedReader(new InputStreamReader(
            socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // Layout GUI
        messageLabel.setBackground(Color.lightGray);
        frame.getContentPane().add(messageLabel, "South");

        JPanel boardPanel = new JPanel();
        boardPanel.setBackground(Color.black);
        boardPanel.setLayout(new GridLayout(3, 3, 2, 2));
        for (int i = 0; i < board.length; i++) {
            final int j = i;
            board[i] = new Square();
            board[i].addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    currentSquare = board[j];
                    out.println("MOVE " + j);}});
            boardPanel.add(board[i]);
        }
        frame.getContentPane().add(boardPanel, "Center");
    }

    /**
     * O thread principal do cliente vai ouvir mensagens
      * A partir do servidor. A primeira mensagem será um "WELCOME"
      * Mensagem em que recebemos a nossa marca. Então nós entramos em um
      * Loop de escuta para "VALID_MOVE", "OPPONENT_MOVED", "vitória",
      * "Derrota", "tie", "OPPONENT_QUIT ou" mensagens de mensagem ",
      * E manipulação de cada mensagem adequada. A "vitória",
      * "Derrota" e "Laço" perguntar ao usuário se deve ou não jogar
      * Outro jogo. Se a resposta for não, o loop é encerrado e
      * O servidor é enviada uma mensagem "SAIR". Se um OPPONENT_QUIT
      * Mensagem é recevied seguida, o loop será encerrado eo servidor
      * Será enviada uma mensagem "SAIR" também.
     */
    public void play() throws Exception {
        String response;
        try {
            response = in.readLine();
            if (response.startsWith("WELCOME")) {
                char mark = response.charAt(8);
                if(mark == 'X' || true){
                	 icon = new ImageIcon(getClass().getResource("xis.gif"));
                     opponentIcon = new ImageIcon(getClass().getResource("zero.gif"));
                }
                
                //icon = new ImageIcon(mark == 'X' ? "xis.gif" : "zero.gif");
                //opponentIcon  = new ImageIcon(mark == 'X' ? "zero.gif" : "xis.gif");
                frame.setTitle("Jogo da Velha - " + mark);
            }
            while (true) {
                response = in.readLine();
                if (response.startsWith("VALID_MOVE")) {
                    messageLabel.setText("OK, aguarde o oponente");
                    currentSquare.setIcon(icon);
                    currentSquare.repaint();
                } else if (response.startsWith("OPPONENT_MOVED")) {
                    int loc = Integer.parseInt(response.substring(15));
                    board[loc].setIcon(opponentIcon);
                    board[loc].repaint();
                    messageLabel.setText("É a sua vez");
                } else if (response.startsWith("VICTORY")) {
                    messageLabel.setText("Você ganhou");
                    break;
                } else if (response.startsWith("DEFEAT")) {
                    messageLabel.setText("Você perdeu");
                    break;
                } else if (response.startsWith("TIE")) {
                    messageLabel.setText("Você foi amarrado");
                    break;
                } else if (response.startsWith("MESSAGE")) {
                    messageLabel.setText(response.substring(8));
                }
            }
            out.println("QUIT");
        }
        finally {
            socket.close();
        }
    }

    private boolean wantsToPlayAgain() {
        int response = JOptionPane.showConfirmDialog(frame,
            "Jogar novamente?",
            "Tic Tac Toe is Fun Fun Fun",
            JOptionPane.YES_NO_OPTION);
        frame.dispose();
        return response == JOptionPane.YES_OPTION;
    }

    /**
     * Graphical square in the client window.  Each square is
     * a white panel containing.  A client calls setIcon() to fill
     * it with an Icon, presumably an X or O.
     */
    static class Square extends JPanel {
        JLabel label = new JLabel((Icon)null);

        public Square() {
            setBackground(Color.white);
            add(label);
        }

        public void setIcon(Icon icon) {
            label.setIcon(icon);
            //label.setText("F");
        }
    }

    /**
     * Runs the client as an application.
     */
    public static void main(String[] args) throws Exception {
        while (true) {
        	
            String serverAddress = (args.length == 0) ? "localhost" : args[1];
            ClienteJogo client = new ClienteJogo(serverIP);
            client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            client.frame.setSize(240, 200);
            client.frame.setVisible(true);
            client.frame.setResizable(false);
            client.play();
            if (!client.wantsToPlayAgain()) {
                break;
            }
        }
    }
}