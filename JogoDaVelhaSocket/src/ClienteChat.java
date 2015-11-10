import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.*;
import javax.swing.border.EmptyBorder;


public class ClienteChat extends JFrame {

    BufferedReader in;
    PrintWriter out;
    JPanel contentPane;
    //JFrame frame = new JFrame("Chatter");
    JTextField textField = new JTextField(60);
    JTextArea messageArea = new JTextArea(12, 60);
    JScrollPane scrollPane = new JScrollPane();
    String meuNome;
    String serverIP;
    DefaultListModel model;
    JList listaPessoas = new JList();

    
    public ClienteChat() {

        // Layout GUI
    	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	setTitle("Jogo da Velha - [Lobby]");
		setBounds(100, 100, 570, 400);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
        textField.setEditable(false);
        messageArea.setEditable(false);

        //Área das mensagens
        scrollPane = new JScrollPane(messageArea);
        messageArea.setBounds(10, 10, 380, 300);
        scrollPane.setBounds(10, 10, 380, 300);
        contentPane.add(scrollPane);
        scrollPane.setVerticalScrollBarPolicy ( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
        
        //Campo de mensagem nova
        textField.setBounds(10, 320, 410, 30);
        contentPane.add(textField);
        
		model = new DefaultListModel();
	    listaPessoas = new JList(model);
	    model.addElement("Desconectado"); 
	    
	    //model.removeAllElements();
		
		listaPessoas.setBounds(400, 10, 140, 300);
		contentPane.add(listaPessoas);
		
        // Add Listeners
        textField.addActionListener(new ActionListener() {
            /**
             * Responds to pressing the enter key in the textfield by sending
             * the contents of the text field to the server.    Then clear
             * the text area in preparation for the next message.
             */
            public void actionPerformed(ActionEvent e) {
            	
            	//Comandos internos
            	if(textField.getText().equals("/limpartela")){
            		messageArea.setText("        ");
            		textField.setText("");
            		return;
            	}
            	
            	if(textField.getText().equals("/jogardireto")){
            		new JogoDaVelhaDireto().setVisible(true);
            		textField.setText("");
            		return;
            	}
            	
                out.println(textField.getText());
                textField.setText("");
            }
        });
        

        listaPessoas.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JList listaPessoas = (JList)evt.getSource();
                if (evt.getClickCount() == 2) {// Double-click detected
                	
                	if(listaPessoas.getSelectedValue().toString().equals(meuNome)) {
                		messageArea.append("\n[INFO] Você não pode enviar um pedido para si mesmo.");
                	} else {
                		out.println("REQUESTGAME "+ listaPessoas.getSelectedValue().toString());
                		messageArea.append("\n[INFO] Você enviou um pedido de jogo para " +  listaPessoas.getSelectedValue().toString() );
                	}
                } /*else if (evt.getClickCount() == 3) {
                    // Triple-click detected
                    int index = listaPessoas.locationToIndex(evt.getPoint());
                }*/
            }
        });
    }

    /**
     * Obtenção do IP do servidor.
     */
    private String getServerAddress() {
        serverIP = (String) JOptionPane.showInputDialog(
            this,
            "Digite o endereço do servidor:",
            "Bem-vindo ao jogo da velha",
            JOptionPane.QUESTION_MESSAGE, null, null, "tsxvpsbr.dyndns.org");
        if (serverIP == null) dispose();
        return serverIP;
    }

    /**
     * Obtenção do Nick do Jogador.
     */
    private String getNick() {
    	meuNome = (String) JOptionPane.showInputDialog(
	            this,
	            "Digite um nome de jogador único que não esteja em uso:",
	            "Seleção de nome de jogador",
	            JOptionPane.PLAIN_MESSAGE);
    	if (meuNome == null) dispose();
    	while(meuNome.equals("") || meuNome.contains(" ") ){
	        meuNome = (String) JOptionPane.showInputDialog(
	            this,
	            "Digite um nome de jogador único que não esteja em uso:\nNão use espaços nem caracteres especiais, apenas A-z 0-9",
	            "Seleção de nome de jogador",
	            JOptionPane.PLAIN_MESSAGE);
	    if (meuNome == null) dispose();
    	}
        return meuNome;
    }

    /**
     * Conecta ao servidor e abre a ouvidoria.
     */
    private void run() throws IOException {

        //Bloco de conexão
    	Boolean tudoOk = false;
    	while(!tudoOk){
	        String serverAddress = getServerAddress();
	        if (serverAddress == null) return;
	        
	        try {
	        	Socket socket = new Socket(serverAddress, 9001);
	        	in = new BufferedReader(new InputStreamReader(
	        		socket.getInputStream()));
	            out = new PrintWriter(socket.getOutputStream(), true);
	            tudoOk = true;
	        } catch (Exception e) {
	        	tudoOk = false;
	        	JOptionPane.showMessageDialog(null, "Falha ao conectar ao servidor. \nVerifique se o endereço está correto:\n\""+
	        			serverAddress +
	        			"\"\nCódigo de erro: "+e.getMessage(),
	        		    "Erro de conexão",
	        		    JOptionPane.ERROR_MESSAGE);
	        }
    	}

        // Process all messages from server, according to the protocol.
        while (true) {
            String line = in.readLine();
            if (line.startsWith("SUBMITNAME")) {
                out.println(getNick());
            } else if (line.startsWith("NAMEACCEPTED")) {
                textField.setEditable(true);
                messageArea.append("Conectado com sucesso como "+meuNome+" !");
                textField.requestFocus();
            } else if (line.startsWith("GAMEINVITE")) {
                
            	String partes[] = line.split(" <> ");//Divide a string recebida pelo socket para fazer a verificação
            	if(partes[2].equals(meuNome)) {//Se a mensagem for direcionada ao cliente escolhido, procede a abertura do pedido.
            		String teste = "[INFO] Você recebeu um pedido de jogo de "+ partes[1] ;
            		//String teste = "[INFO] Você "+ partes[2] +" recebeu um pedido de jogo de "+ partes[1] ;
            		messageArea.append("\n" + teste);
            		messageArea.setCaretPosition(messageArea.getDocument().getLength());
            		Object[] options = {"Aceitar",
                            "Rejeitar"};
			        int resposta = JOptionPane.showOptionDialog(this,
			            partes[1] + " lhe convidou para uma partida.",
			            "Convite de jogo recebido!",
			            JOptionPane.YES_NO_CANCEL_OPTION,
			            JOptionPane.QUESTION_MESSAGE,
			            null,
			            options,
			            options[1]);
			        
			        //messageArea.append("    " + resposta + "   ");
			        
			        if(resposta == 1) {
			        	//REJEITOU. 
			        } else if (resposta == 0) {
			        	//ACEITOU. Abre o jogo local e manda pedido remoto para abrir no cliente.
			        	out.println("OPENGAME "+partes[1]);
			        	try{
			        		ClienteJogo jogo = new ClienteJogo(serverIP);
			        		jogo.getFrame().setSize(240, 200);
			        		jogo.getFrame().setVisible(true);
			        		jogo.getFrame().setResizable(false);
			        		jogo.play();
			        	} catch (Exception e) {};
			        }
            	}

            } else if (line.startsWith("OPENGAME")) {
            	if(line.substring(9).equals(meuNome)){
            		try{
            			ClienteJogo jogo = new ClienteJogo(serverIP);
		        		jogo.getFrame().setSize(240, 200);
		        		jogo.getFrame().setVisible(true);
		        		jogo.getFrame().setResizable(false);
		        		jogo.play();
		        	} catch (Exception e) {};
            	}
            	

            } else if (line.startsWith("MESSAGE")) {
                
            	String mensagemRecebida = line.substring(8);
            	messageArea.append("\n" + mensagemRecebida);
                messageArea.setCaretPosition(messageArea.getDocument().getLength());

            } else if (line.startsWith("REFRESHLISTSTART")) {
            	model.removeAllElements();
            } else if (line.startsWith("REFRESHLISTADD")) {
                //messageArea.append(line.substring(12) + "\n");
                model.addElement(line.substring(15));
            }
            
        }
    
    }

    /**
     * Construção da interface do aplicativo.
     */
    public static void main(String[] args) throws Exception {
        ClienteChat client = new ClienteChat();
        client.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.setVisible(true);
        client.run();
    }
}