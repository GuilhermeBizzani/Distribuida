/*  
TIC TAC TOE - XO Multiplayer Game
visit: youtube.com/defektruke
by Aleksandar Dj.

Ultimo updates:

10/11 21:58:
Eu baixei


23/11 22:04
adicionado thread do servidor

*/

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.List;
import java.awt.TextArea;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;



public class JogoDaVelhaDireto extends JFrame
{
	
	//private Threads hue = new Threads();
	
	
	private static final long serialVersionUID = 1L;
	
	//ServerSocket, Socket, Input and Output Streams
	private ServerSocket serverSocket = null;
	private Socket conectar = null;
	private ObjectInputStream entrada = null;
	private ObjectOutputStream saida = null;

	public static ArrayList<String> listaPessoasOnline = new ArrayList<String>();
	public static String meuNick;
	
	private Dimension screenSize;									// screen size
	private int width;												// width of screen
	private int height;												// height of screen
	
	private JButton b1, b2, b3, b4, b5, b6, b7, b8, b9;				// buttons XO fields
	/*
	 Button position:
	 [b1][b2][b3]
	 [b4][b5][b6]
	 [b7][b8][b9]
	 */
	
	private static JTextArea textArea;										// text area on right side of frame for chat and notifications
	public static JTextArea textAreaa;									// text area on right side of frame for chat and notifications
	private JScrollPane sp;											// scroll pane for text area
	
	private static JTextField ip; 					// IP address, port number, nickname, chat message
	public static String meuIPLocal;
	
	private static JTextField port;

	private static JTextField nick;

	private JTextField message;
	private JButton join, create, peer, novaPartija; 						// buttons : JOIN, CREATE, NEW GAME
	
	
	private String campo[] = { "","","", "","","", "","","" }; 		// FIELDS XO (see example in multiline comment)
	/*
	Explanation:
	for ex. if campo = { "X", "X", "X", ...}; then X win
	[X][X][X]
	[ ][ ][ ]
	[ ][ ][ ]
	2nd ex. if campo = { "O","","", "","O","", "","","O" };
	[O][ ][ ]
	[ ][O][ ]
	[ ][ ][O]
	 */
	
	private String xo = ""; 										// server is X , client is O
	private String nick1, nick2, mensagem; 							// nick1 server, nick2 client, chat message
	
	private boolean signal; 										// signal for "WHOSE TURN"
	private boolean possueMensagens = true;							// if this signal is false, then stop sending messages over Internet
	
	private int jogadorAtual = 0;										//The number of parties // 0=X play first; 1=O play 2nd; 3=X turn....
	private int numDeJogadas = 0;										//The number of moves, if number is higher or equal to 9 , game is draw
	
	private Font fontText, fontButtons; 							// Fonts
	
	private String safeSing = "!pass123!#$%&/()!";					// i add this sing, its something like password, when data is transfer over ip
	
	// --- Constructor ---
	public JogoDaVelhaDireto()
	{
		initUI();
		
	}
	
	// --- User Interface ---
	private void initUI()
	{
		try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) { } // get&set system UI
		
		screenSize = Toolkit.getDefaultToolkit().getScreenSize();	 // get screen size
		width = (int) screenSize.getWidth(); 						 // get width
		height = (int) screenSize.getHeight(); 						 // get height
		
		setSize(width/2, height/2); 								 // set the size of a frame to 1/3 of a window screen
		setResizable(true);
		setTitle("XO-DemoGame");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		
		setLayout(new BorderLayout());
		
		fontText = new Font("Book Antiqua", Font.PLAIN, 30);
		fontButtons = new Font("Book Antiqua", Font.PLAIN, 18);
		
		// --- CENTAR PANEL ----
		// panel for xo buttons
		JPanel pCenter = new JPanel();
		pCenter.setLayout(new GridLayout(3, 3));
				
		b1 = new JButton("[ ]");
		b1.setFont(fontText);
		b1.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				if(signal)
				{
					b1.setText(xo); // set X or O button text
					enviarMsg(xo + "1" + safeSing);
					enviarMsg("true" + safeSing);
					signal = false;
					b1.setEnabled(false);
					if(xo.equals("X"))
					{
						campo[0] = "X";
					}
					else
					{
						campo[0] = "O";
					}
					++numDeJogadas;
					proveriTabelu();
				}
			}
		});
		pCenter.add(b1);
		
		b2 = new JButton("[ ]");
		b2.setFont(fontText);
		b2.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				if(signal)
				{
					b2.setText(xo);
					enviarMsg(xo + "2" + safeSing);
					enviarMsg("true" + safeSing);
					signal = false;
					b2.setEnabled(false);
					if(xo.equals("X"))
					{
						campo[1] = "X";
					}
					else
					{
						campo[1] = "O";
					}
					++numDeJogadas;
					proveriTabelu();
				}
			}
		});
		pCenter.add(b2);
		
		b3 = new JButton("[ ]");
		b3.setFont(fontText);
		b3.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				if(signal)
				{
					b3.setText(xo);
					enviarMsg(xo + "3" + safeSing);
					enviarMsg("true" + safeSing);
					signal = false;
					b3.setEnabled(false);
					if(xo.equals("X"))
					{
						campo[2] = "X";
					}
					else
					{
						campo[2] = "O";
					}
					++numDeJogadas;
					proveriTabelu();
				}
			}
		});
		pCenter.add(b3);
		
		b4 = new JButton("[ ]");
		b4.setFont(fontText);
		b4.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				if(signal)
				{
					b4.setText(xo);
					enviarMsg(xo + "4" + safeSing);
					enviarMsg("true" + safeSing);
					signal = false;
					b4.setEnabled(false);
					if(xo.equals("X"))
					{
						campo[3] = "X";
					}
					else
					{
						campo[3] = "O";
					}
					++numDeJogadas;
					proveriTabelu();
				}
			}
		});
		pCenter.add(b4);
		
		b5 = new JButton("[ ]");
		b5.setFont(fontText);
		b5.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				if(signal)
				{
					b5.setText(xo);
					enviarMsg(xo + "5" + safeSing);
					enviarMsg("true" + safeSing);
					signal = false;
					b5.setEnabled(false);
					if(xo.equals("X"))
					{
						campo[4] = "X";
					}
					else
					{
						campo[4] = "O";
					}
					++numDeJogadas;
					proveriTabelu();
				}
			}
		});
		pCenter.add(b5);
		
		b6 = new JButton("[ ]");
		b6.setFont(fontText);
		b6.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				if(signal)
				{
					b6.setText(xo);
					enviarMsg(xo + "6" + safeSing);
					enviarMsg("true" + safeSing);
					signal = false;
					b6.setEnabled(false);
					if(xo.equals("X"))
					{
						campo[5] = "X";
					}
					else
					{
						campo[5] = "O";
					}					
					++numDeJogadas;
					proveriTabelu();
				}
			}
		});
		pCenter.add(b6);
		
		b7 = new JButton("[ ]");
		b7.setFont(fontText);
		b7.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				if(signal)
				{
					b7.setText(xo);
					enviarMsg(xo + "7" + safeSing);
					enviarMsg("true" + safeSing);
					signal = false;
					b7.setEnabled(false);
					if(xo.equals("X"))
					{
						campo[6] = "X";
					}
						
					else
					{
						campo[6] = "O";
					}
					++numDeJogadas;
					proveriTabelu();
				}
			}
		});
		pCenter.add(b7);
		
		b8 = new JButton("[ ]");
		b8.setFont(fontText);
		b8.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				if(signal)
				{
					b8.setText(xo);
					enviarMsg(xo + "8" + safeSing);
					enviarMsg("true" + safeSing);
					signal = false;
					b8.setEnabled(false);
					if(xo.equals("X"))
					{
						campo[7] = "X";
					}
					else
					{
						campo[7] = "O";
					}
					++numDeJogadas;
					proveriTabelu();
				}
			}
		});
		pCenter.add(b8);
		
		b9 = new JButton("[ ]");
		b9.setFont(fontText);
		b9.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				if(signal)
				{
					b9.setText(xo);
					enviarMsg(xo + "9" + safeSing);
					enviarMsg("true" + safeSing);
					signal = false;
					b9.setEnabled(false);
					if(xo.equals("X"))
					{
						campo[8] = "X";
					}
					else
					{
						campo[8] = "O";
					}
					++numDeJogadas;
					proveriTabelu();
				}	
			}
		});
		pCenter.add(b9);
		
		resetaCampos(false); 	// set all buttons on false till we wait for client to join
		add(pCenter, BorderLayout.WEST);
		
		// --- PANEL DE CHAT E INFORMAÇÕES ---
		JPanel pEast = new JPanel();
		pEast.setLayout(new BorderLayout());
		pEast.setPreferredSize(new Dimension(270, height));
		textArea = new JTextArea();
		textArea.setLineWrap(true);
		textArea.setEditable(false);
		textArea.setFont(fontButtons);
		textArea.append("Game started\n"); //First line of the chat
		sp = new JScrollPane(textArea); 
		sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		pEast.add(sp, BorderLayout.CENTER);
		add(pEast, BorderLayout.CENTER);
		
		
		// ---PAINEL CONTENDO OS CLIENTES QUE ESTÃO ONLINE---
		JPanel pLista = new JPanel();
		pLista.setLayout(new BorderLayout());
		pLista.setPreferredSize(new Dimension(270, height));
		textAreaa = new JTextArea();
		textAreaa.setLineWrap(true);
		textAreaa.setEditable(false);
		textAreaa.setFont(fontButtons);
		textAreaa.append("Iniciando...\n"); //First line of the chat
		JScrollPane spp = new JScrollPane(textAreaa); 
		spp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		pLista.add(spp, BorderLayout.CENTER);
		add(pLista, BorderLayout.EAST);
		
		
		// --- SOUTH PANEL ---
		JPanel pSouth = new JPanel();
		pSouth.setLayout(new BorderLayout());
		pSouth.setPreferredSize(new Dimension(width/3, 50));
		message = new JTextField(" ");
		message.setEditable(false);
		message.setFont(fontText);
		message.addKeyListener(new KeyAdapter()
		{
			public void keyPressed(KeyEvent e) 
			{
				if(e.getKeyCode() == KeyEvent.VK_ENTER)
				{
					textArea.append(nick.getText() + ":" + message.getText() + "\n");
					scrollToBottom();
					enviarMsg(message.getText());
					message.setText(" ");
				}
			}
		});
		pSouth.add(message, BorderLayout.CENTER);
		add(pSouth, BorderLayout.SOUTH);
		
		ip = new JTextField("127.0.0.1");
		ip.setToolTipText("Enter Host IP addres");
		ip.setPreferredSize(new Dimension(100, 25));
		Random gerador = new Random();
		int porta = (gerador.nextInt(1000)) + 9000;
		port = new JTextField(""+porta);
		port.setToolTipText("Enter Host PORT nubmer, default:9876");
		port.setPreferredSize(new Dimension(100, 25));
		nick = new JTextField(meuNick);
		nick.setEditable(false);
		nick.setToolTipText("Enter your Nickname");
		nick.setPreferredSize(new Dimension(100, 25));
		
		
		
		// --- PEER BUTTON ---
		peer = new JButton("Peer");
		peer.setToolTipText("Connect to peer");
		peer.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent event) 
			{
				if(nick.getText().equals("") || nick.getText().equals(" "))
				{
					try { JOptionPane.showMessageDialog(null, "You did not input your nickname!"); } catch (ExceptionInInitializerError exc) { }
					return;
				}
				
				new PeerButtonThread("PeerButton"); // we need thread while we wait for client, because we don't want frozen frame

			}
		});
		
		
		// --- CREATE BUTTON ---
		create = new JButton("Create");
		create.setToolTipText("Create game");
		create.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent event) 
			{
				if(nick.getText().equals("") || nick.getText().equals(" "))
				{
					try { JOptionPane.showMessageDialog(null, "You did not input your nickname!"); } catch (ExceptionInInitializerError exc) { }
					return;
				}
				
				new CreateButtonThread("CreateButton"); // we need thread while we wait for client, because we don't want frozen frame

			}
		});
	
		// --- JOIN BUTTON ---
		join = new JButton("Join");
		join.setToolTipText("Join remote game");
		join.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent event) 
			{
				try
				{ 	
					if(nick.getText().equals("") || nick.getText().equals(" "))
					{
						try { JOptionPane.showMessageDialog(null, "You did not input your nickname!"); } catch (ExceptionInInitializerError exc) { }
						return;
					}
					
					conectar = new Socket(ip.getText(), Integer.parseInt(port.getText())); 
					
					saida = new ObjectOutputStream(conectar.getOutputStream());
					saida.flush();
					entrada = new ObjectInputStream(conectar.getInputStream());
					
					mensagem = (String) entrada.readObject();
					textArea.append(mensagem + "\n");
					scrollToBottom();
					
					xo = "O";
					signal = false;
					
					nick2 = nick.getText();
					
					mensagem = (String) entrada.readObject(); // get nick from host
					nick1 = "" + mensagem;
					
					enviarMsg(nick2);
					
					resetaCampos(true);
					message.setEditable(true);
					
					ip.setEnabled(false);
					port.setEnabled(false);
					nick.setEnabled(false);
					
					textArea.append("X plays first!\n");
					scrollToBottom();
					
					join.setEnabled(false);
					create.setEnabled(false);
					peer.setEnabled(true);
					ip.setEnabled(false);
					port.setEnabled(false);
					nick.setEnabled(false);
					
					new IniciarJogo("mensagemDoServidor"); // thread for receive data from host		
				}
				catch(Exception e)
				{
					finaliza();
					reinicia();
					try { JOptionPane.showMessageDialog(null, "JoinButton: Error: Server is offline: \n" + e); } catch (ExceptionInInitializerError exc) { }
				}
			}
		});
		
		// --- DUGME ZA NOVU PARTIJU - BUTTON FOR NEW GAME ---
		novaPartija = new JButton("New Game");
		novaPartija.setToolTipText("Play a new game");
		novaPartija.setEnabled(false);
		novaPartija.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				enviarMsg("SolicitarNovaPartida!" + safeSing); // send request to client, for new game
				
				++jogadorAtual;
				
				for (int i=0; i<campo.length; i++)
				{
					campo[i] = "";
				}
				
				if(jogadorAtual %2 == 0)
				{
					signal = true;
					textArea.append("X  plays first!\n");
					scrollToBottom();
					enviarMsg("false" + safeSing);
					enviarMsg("X  plays first!");
				}
				else 
				{
					signal = false;
					enviarMsg("true" + safeSing);
					textArea.append("O plays first!\n");
					scrollToBottom();
					enviarMsg("O  plays first!");
				}
				
				postaviTekstPolja();
				resetaCampos(true);
				novaPartija.setEnabled(false);
			}
		});
		
		JPanel pNorth = new JPanel();
		pNorth.add(peer);
		pNorth.add(ip);
		pNorth.add(port);
		pNorth.add(nick);
		pNorth.add(create);
		pNorth.add(join);
		pNorth.add(novaPartija);
		add(pNorth, BorderLayout.NORTH);
		
		// --- WINDOW ADAPTER ---
		addWindowListener(new WindowAdapter()
		{
			public void windowActivated(WindowEvent event) 
			{
				try 
				{
					InetAddress thisIp = InetAddress.getLocalHost();
					meuIPLocal = thisIp.getHostAddress();
					ip.setText(meuIPLocal);
				} 
				catch (Exception e) 
				{ 
					ip.setText("127.0.0.1"); 
				}
			}	
			
			public void windowClosing(WindowEvent event) 
			{
				if(conectar != null) 
				{
					enviarMsg("Going offline!");
				}
				finaliza();
			}
		});
	}
	
	// --- CREATE BUTTON THREAD ---
	private class CreateButtonThread implements Runnable
	{	
		public CreateButtonThread(String name)
		{
			new Thread(this, name).start();
		}
		
		public void run()
		{
			try 
			{
				join.setEnabled(false);
				create.setEnabled(false);
				port.setEnabled(false);
				nick.setEnabled(false);
				
				serverSocket = new ServerSocket(Integer.parseInt(port.getText())); 
				
				textArea.append("Waiting for client...\n");
				scrollToBottom();
				
				conectar = serverSocket.accept();
				
				saida = new ObjectOutputStream(conectar.getOutputStream());
				saida.flush();
				entrada = new ObjectInputStream(conectar.getInputStream());
				enviarMsg(nick.getText() +  ": Successfully connected!");
				textArea.append("Client Successfully connected!\n");
				scrollToBottom();
				
				xo = "X";
				signal = true;
				
				nick1 = nick.getText();
				
				enviarMsg(nick1);
				
				mensagem = (String) entrada.readObject(); // prima NICK OD SERVERA
				nick2 = "" + mensagem;
				
				resetaCampos(true);
				message.setEditable(true);
				ip.setEnabled(false);
				
				textArea.append("X plays first!\n");
				scrollToBottom();
				new IniciarJogo("mensagemDoCliente"); 
			}
			catch (Exception e) 
			{ 
				finaliza();
				reinicia();
				try { JOptionPane.showMessageDialog(null, "CreateButton: Error while creating game:\n" + e);  } catch (ExceptionInInitializerError exc) { }
			}
		}
	}
	
	// --- CHECK FIELDS --- 
	private void proveriTabelu()
	{
		// ------_X_X_X_------
		if
		(
			// CHECK X POSITIONS - VERTICAL
			(campo[0].equals("X") && campo[1].equals("X") && campo[2].equals("X")) || 
			(campo[3].equals("X") && campo[4].equals("X") && campo[5].equals("X")) ||
			(campo[6].equals("X") && campo[7].equals("X") && campo[8].equals("X")) ||
			// CHECK X POSITIONS - HORIZONTAL
			(campo[0].equals("X") && campo[3].equals("X") && campo[6].equals("X")) ||
			(campo[1].equals("X") && campo[4].equals("X") && campo[7].equals("X")) ||
			(campo[2].equals("X") && campo[5].equals("X") && campo[8].equals("X")) ||
			// CHECK X POSITIONS - DIAGONAL
			(campo[0].equals("X") && campo[4].equals("X") && campo[8].equals("X")) ||
			(campo[2].equals("X") && campo[4].equals("X") && campo[6].equals("X"))
		)
		{
			numDeJogadas = 0;
			resetaCampos(false);
			JOptionPane.showMessageDialog(null, nick1 + " je pobedio! WIN!");
			if(xo.equals("X")) { novaPartija.setEnabled(true); }
		}
		// ------_O_O_O_------
		else if
		(
				// CHECK O POSITIONS - HORIZONTAL
				(campo[0].equals("O") && campo[1].equals("O") && campo[2].equals("O")) ||
				(campo[3].equals("O") && campo[4].equals("O") && campo[5].equals("O")) ||
				(campo[6].equals("O") && campo[7].equals("O") && campo[8].equals("O")) ||
				// CHECK O POSITIONS - VERTICAL
				(campo[0].equals("O") && campo[3].equals("O") && campo[6].equals("O")) ||
				(campo[1].equals("O") && campo[4].equals("O") && campo[7].equals("O")) ||
				(campo[2].equals("O") && campo[5].equals("O") && campo[8].equals("O")) ||
				// CHECK O POSITIONS - DIAGONAL
				(campo[0].equals("O") && campo[4].equals("O") && campo[8].equals("O")) ||
				(campo[2].equals("O") && campo[4].equals("O") && campo[6].equals("O"))
		)
		{
			numDeJogadas = 0;
			resetaCampos(false);
			JOptionPane.showMessageDialog(null, nick2 + " je pobedio! WIN!");
			if(xo.equals("X")) { novaPartija.setEnabled(true); }
		}
		else
		{
			//CHECK IF IS DRAW
			if(numDeJogadas >= 9)
			{
				numDeJogadas = 0;
				enviarMsg("NERESENO!" + safeSing);
				JOptionPane.showMessageDialog(null, "DRAW/NERESENO!");
				if(xo.equals("X")) { novaPartija.setEnabled(true); }
			}
		}
	}

	// --- enable/disable buttons ---
	private void resetaCampos(boolean b)
	{
		b1.setEnabled(b);
		b2.setEnabled(b);
		b3.setEnabled(b);
		b4.setEnabled(b);
		b5.setEnabled(b);
		b6.setEnabled(b);
		b7.setEnabled(b);
		b8.setEnabled(b);
		b9.setEnabled(b);		
	}
	
	// --- set default text state to buttons ---
	private void postaviTekstPolja()
	{
		b1.setText("[ ]");
		b2.setText("[ ]");
		b3.setText("[ ]");
		b4.setText("[ ]");
		b5.setText("[ ]");
		b6.setText("[ ]");
		b7.setText("[ ]");
		b8.setText("[ ]");
		b9.setText("[ ]");
	}
	
	// --- Send Data over Internet ---
	private void enviarMsg(String p)
	{			
		try
		{
			if(possueMensagens)
			{
				saida.writeObject(p);
				saida.flush();
			}
		}
		catch(SocketException e)
		{
			if(possueMensagens)
			{
				possueMensagens = false;
				finaliza();
				reinicia();
			}
		}
		catch(Exception e) 
		{ 
			if(possueMensagens)
			{
				possueMensagens = false;
				finaliza();
				reinicia();
				try { JOptionPane.showMessageDialog(null, "Sending data/Disconnect:\n" + e); } catch (ExceptionInInitializerError exc) { }
			}
		}
	}
	
	// --- Receive data/messages thread ---
	private class IniciarJogo implements Runnable
	{	
		private boolean nitSig;
		private String imeNiti;
		
		public IniciarJogo(String i)
		{
			nitSig = true;
			imeNiti = i;
			new Thread(this, imeNiti).start();
		}
		
		public void run()
		{

			while(nitSig)
			{
				try
				{
					mensagem = "";
					mensagem = (String) entrada.readObject();			// receive messages
					
					if(imeNiti.equals("mensagemDoServidor")) 			// client receive data from host/server
					{
						if(mensagem.equalsIgnoreCase("true" + safeSing))
						{
							signal = true;
						}
						else if(mensagem.equalsIgnoreCase("false" + safeSing))
						{
							signal = false;
						}
						else if(mensagem.equalsIgnoreCase("NERESENO!" + safeSing))
						{
							JOptionPane.showMessageDialog(null, "DRAW/NERESENO!");
						}
						else if(mensagem.equalsIgnoreCase("SolicitarNovaPartida!" + safeSing))
						{
							for (int i=0; i<campo.length; i++)
							{
								campo[i] = "";
							}
							signal = true;
							postaviTekstPolja();
							resetaCampos(true);
						}
						else if(mensagem.equalsIgnoreCase("X1" + safeSing))
						{
							b1.setText("X");
							campo[0] = "X";
							b1.setEnabled(false);
							proveriTabelu();
						}
						else if(mensagem.equalsIgnoreCase("X2" + safeSing))
						{
							b2.setText("X");
							campo[1] = "X";
							b2.setEnabled(false);
							proveriTabelu();
						}
						else if(mensagem.equalsIgnoreCase("X3" + safeSing))
						{
							b3.setText("X");
							campo[2] = "X";
							b3.setEnabled(false);
							proveriTabelu();
						}
						else if(mensagem.equalsIgnoreCase("X4" + safeSing))
						{
							b4.setText("X");
							campo[3] = "X";
							b4.setEnabled(false);
							proveriTabelu();
						}
						else if(mensagem.equalsIgnoreCase("X5" + safeSing))
						{
							b5.setText("X");
							campo[4] = "X";
							b5.setEnabled(false);
							proveriTabelu();
						}
						else if(mensagem.equalsIgnoreCase("X6" + safeSing))
						{
							b6.setText("X");
							campo[5] = "X";
							b6.setEnabled(false);
							proveriTabelu();
						}
						else if(mensagem.equalsIgnoreCase("X7" + safeSing))
						{
							b7.setText("X");
							campo[6] = "X";
							b7.setEnabled(false);
							proveriTabelu();
						}
						else if(mensagem.equalsIgnoreCase("X8" + safeSing))
						{
							b8.setText("X");
							campo[7] = "X";
							b8.setEnabled(false);
							proveriTabelu();
						}
						else if(mensagem.equalsIgnoreCase("X9" + safeSing))
						{
							b9.setText("X");
							campo[8] = "X";
							b9.setEnabled(false);
							proveriTabelu();
						}
						else
						{
							if(mensagem.endsWith(safeSing))
							{
								mensagem = mensagem.substring(0, mensagem.length() - safeSing.length());
							}
							textArea.append(nick1 + ":" + mensagem + "\n");
							scrollToBottom();
						}
					}
					else if(imeNiti.equals("mensagemDoCliente"))			// host/server receive data from client
					{
						if(mensagem.equalsIgnoreCase("true" + safeSing))
						{
							signal = true;
						}
						else if(mensagem.equalsIgnoreCase("O1" + safeSing))
						{
							++numDeJogadas;
							b1.setText("O");
							campo[0] = "O";
							b1.setEnabled(false);
							proveriTabelu();
						}
						else if(mensagem.equalsIgnoreCase("O2" + safeSing))
						{
							++numDeJogadas;
							b2.setText("O");
							campo[1] = "O";
							b2.setEnabled(false);
							proveriTabelu();
						}
						else if(mensagem.equalsIgnoreCase("O3" + safeSing))
						{
							++numDeJogadas;
							b3.setText("O");
							campo[2] = "O";
							b3.setEnabled(false);
							proveriTabelu();
						}
						else if(mensagem.equalsIgnoreCase("O4" + safeSing))
						{
							++numDeJogadas;
							b4.setText("O");
							campo[3] = "O";
							b4.setEnabled(false);
							proveriTabelu();
						}
						else if(mensagem.equalsIgnoreCase("O5" + safeSing))
						{
							++numDeJogadas;
							b5.setText("O");
							campo[4] = "O";
							b5.setEnabled(false);
							proveriTabelu();
						}
						else if(mensagem.equalsIgnoreCase("O6" + safeSing))
						{
							++numDeJogadas;
							b6.setText("O");
							campo[5] = "O";
							b6.setEnabled(false);
							proveriTabelu();
						}
						else if(mensagem.equalsIgnoreCase("O7" + safeSing))
						{
							++numDeJogadas;
							b7.setText("O");
							campo[6] = "O";
							b7.setEnabled(false);
							proveriTabelu();
						}
						else if(mensagem.equalsIgnoreCase("O8" + safeSing))
						{
							++numDeJogadas;
							b8.setText("O");
							campo[7] = "O";
							b8.setEnabled(false);
							proveriTabelu();
						}
						else if(mensagem.equalsIgnoreCase("O9" + safeSing))
						{
							++numDeJogadas;
							b9.setText("O");
							campo[8] = "O";
							b9.setEnabled(false);
							proveriTabelu();
						}
						else
						{
							if(mensagem.endsWith(safeSing))
							{
								mensagem = mensagem.substring(0, mensagem.length() - safeSing.length());
							}
							textArea.append(nick2 + ":" + mensagem + "\n");
							scrollToBottom();
						}
					}
				}
				catch (Exception e)
				{
					nitSig = false;
					finaliza();
					reinicia();
					try { JOptionPane.showMessageDialog(null, "Receiving Data Failed/Disconnect:\n" + e); } catch (ExceptionInInitializerError exc) { }
				}
			}
		}
	}
	
	// --- restart the game to the initial state ---
	private void reinicia()
	{
		postaviTekstPolja();
		resetaCampos(false);
		
		mensagem = "";
		xo = "";
		possueMensagens = true;
		numDeJogadas = 0;
		jogadorAtual = 0;
		
		for (int i=0; i<campo.length; i++)
		{
			campo[i] = "";
		}
		
		ip.setEnabled(true);
		port.setEnabled(true);
		nick.setEnabled(true);
		create.setEnabled(true);
		join.setEnabled(true);
		
		novaPartija.setEnabled(false);
		message.setEditable(false);
	}
	
	// --- Turn OFF all streams ---
	private void finaliza()
	{
		try { saida.flush(); 		} catch (Exception e) { }
		try { saida.close(); 		} catch (Exception e) { }
		try { entrada.close(); 		} catch (Exception e) { }
		try { serverSocket.close();	} catch (Exception e) { }
		try { conectar.close(); 	} catch (Exception e) { }
	}
	
	// --- scroll to bottom when receive chat message ---
	public void scrollToBottom()
	{
		textArea.setCaretPosition(textArea.getText().length());
	}

	
	// --- Main ---
	public static void main(String[] args)
	{

		String nome = null;
		while (nome == null || nome.equals("")) {
			nome = JOptionPane.showInputDialog("Qual o seu nome?");
			if (nome == null || nome.equals("")) {
			JOptionPane.showMessageDialog(null,
			"Você não respondeu a pergunta.");
			}
		}
		//JOptionPane.showMessageDialog(null, "Seu nome é " + nome);
		meuNick = nome;


		
		
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{

				new JogoDaVelhaDireto().setVisible(true);
				Thread t1 = new Thread(new ServidorDePessoas());
				t1.start();
				Thread t2 = new Thread(new AtualizaListaDeOnline());
				t2.start();
			}
		});
	}
	

	
    static void threadMessage(String message) {
        String threadName = Thread.currentThread().getName();
        
        System.out.format("%s: %s%n",
                          threadName,
                          message);
        
    }
	
    
    //essa função caralhuda é responsável por receber as mensagens de outros clientes contendo a lista de conectados deles
    //se encontrar um cliente que não esteja na sua lista, ela o adiciona.
    //verificação de cliente "repetido" feita pelo IP e porta, não pelo nickname.
    public static class ServidorDePessoas implements Runnable {
	    @SuppressWarnings("deprecation")
		public void run() {
	        
	        try {
	        	
	        	
	        	//ServerSocket conectados = new ServerSocket(10101);
	        	
	        	//criar uma lista de clientes conectados
	        	ArrayList<ObjectOutputStream> clientes = new ArrayList<ObjectOutputStream>();
	        	
	        	//fica sempre ouvindo se algum cliente tenta  no servidor
	        	//se algum cliente NOVO , adiciona em CLIENTES
	        	
	        	
	        	Socket conectar2 = null;
	        	ServerSocket conectados;
	        	ObjectInputStream entrada = null;
	        	ObjectOutputStream saida = null;
	        	
            	//serverSocket = new ServerSocket(Integer.parseInt(port.getText())); 
            	conectados = new ServerSocket(10101); 
            	String response;
            	
	           while(true) {
	    	        
	            	try {
						//textArea.append("Aguardando conexao...\n");
						
						conectar2 = conectados.accept();
						
						saida = new ObjectOutputStream(conectar2.getOutputStream());
						saida.flush();
						entrada = new ObjectInputStream(conectar2.getInputStream());
						response = (String)entrada.readObject();
						
						//textArea.append("CLIENTE FALOU: "+ response + "\n");
						
						
						
						if (response.startsWith("ADDCLIENT")) {
							String mensagemRecebida = response.substring(10);
							String partes[] = mensagemRecebida.split("<#> ");
							
							for (int i = 0; i < partes.length; i++) {
								
								//Verifica se a pessoa que ta entrando ja ta na lista
								int achou = 0;
								for (int k = 0; k < listaPessoasOnline.size(); k++) {
									//System.out.println("Ta passando aqui. [éoq|" + listaPessoasOnline.get(k).substring(0, 20) +"|==|"+partes[i].substring(0, 20));
									if (listaPessoasOnline.get(k).substring(0, 20).equals(partes[i].substring(0, 20)) ){
										achou = 1;	//Se tiver alguem, fica 1
										//System.out.println("Achou!!!");
									}
								}
								if (achou == 0){ //Se nao tiver ainda, adiciona
									listaPessoasOnline.add(partes[i]);
								}
							}
							
						//função para ser implementada. detectaria caso algum cliente se desconectou, para o remover da lista
						} else if (response.startsWith("DELETECLIENT")) {
							
						}
						
						//textArea.append("Conexao finalizada\n ");
		            							
						//String partes[] = mensagemRecebida.split(" <> ");
						//textAreaa.append(""+ partes[0] + "\n");
		            	
		            	
		            	System.out.println("Passou na thread de atualizar pessoas");
	            	} catch (Exception e) {
	            		e.printStackTrace();
	            	}

	                Thread.sleep(1000);

	            }
	        } catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
    }
    
    
    //função que envia a mensagem contendo sua lista de clientes para todos os clientes contidos em sua lista.
    //percorre todos os armazenados na lista (menos sí próprio) e envia uma mensagem com a lista completa
    //função também imrpime no TextAreaa a lista de clientes conectados.
    public static class AtualizaListaDeOnline implements Runnable {
	    @SuppressWarnings("deprecation")
		public void run() {

	    	try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	    	
	    	//na primeira iteração, se adiciona na lista.
	    	listaPessoasOnline.add(meuIPLocal +" <> "+ port.getText() +" <> "+ nick.getText() + " <#> ");

	    	while(true){
            	try{
            		Thread.sleep(2000);
            		//toda iteração zera o textAreaa e adiciona os clientes de novo.
            		textAreaa.setText("Online\n");
            		
            		System.out.println(listaPessoasOnline.size());
            		
            		for (int i = 0; i < listaPessoasOnline.size(); i++) {
	            		String partes[] = listaPessoasOnline.get(i).split(" <> ");
	            		
	            		if (partes[0].equals(meuIPLocal)){
	            			//System.out.println("O ip é igual!");
	            			continue;
	            		}
	            		
	            		textAreaa.append(""+ partes[0] + ":");
	            		textAreaa.append(""+ partes[1] + " -> ");
	            		textAreaa.append(""+ partes[2] + "\n");
	    				System.out.println(textAreaa.getText());
	    				
	    				//Mandar pra galera
	    				//Broadcast do IP para todos.
	    				try {
	    					
	    					Socket conectar3 = null;
	    					ObjectOutputStream saida3 = null;
	    					//conectar3 = new Socket(ip.getText(), Integer.parseInt(port.getText()));
	    					conectar3 = new Socket(partes[0], 10101);
	    					
	    					saida3 = new ObjectOutputStream(conectar3.getOutputStream());
	    					saida3.flush();
	    					
	    					String stringMelhor = "ADDCLIENT ";
	    					for (int j = 0; j < listaPessoasOnline.size(); j++) {
	    						stringMelhor = stringMelhor + listaPessoasOnline.get(j);
	    					}
	    					saida3.writeObject(stringMelhor);
	    					
	    					conectar3.close();
	    					
	    				} catch(Exception e) { 
	    					System.out.println(e.getStackTrace());
	    				}
	    				
            		}
                	

                	System.out.println("Passou na thread atualizadora de text fields ");
                	System.out.println("Lista das ppl: "+listaPessoasOnline);
                    // Print a message

            		
            	} catch ( Exception e){
            		e.printStackTrace();
            	}
				
            }
      
	    }
    }
    
    
    
 // --- PEER BUTTON THREAD ---
 	private class PeerButtonThread implements Runnable
 	{	
 		public PeerButtonThread(String name)
 		{
 			new Thread(this, name).start();
 		}
 		
 		public void run()
 		{
 			try 
 			{
 				//peer.setEnabled(false);
 				//listaPessoasOnline.add(meuIPLocal +" <> "+ port.getText() +" <> "+ nick.getText() + " <#> ");
 				
 				//Broadcast do IP para todos.
				try {
					
					Socket conectar3 = null;
					ObjectOutputStream saida3 = null;
					//conectar3 = new Socket(ip.getText(), Integer.parseInt(port.getText()));
					conectar3 = new Socket(ip.getText(), 10101);
					//conectar3 = new Socket("192.168.1.33", 10101);
					
					saida3 = new ObjectOutputStream(conectar3.getOutputStream());
					saida3.flush();
					
					saida3.writeObject("ADDCLIENT "+ meuIPLocal +" <> "+ port.getText() +" <> "+ nick.getText() + " <#> ");
					
					conectar3.close();
					
				} catch(Exception e) { 
					System.out.println(e.getStackTrace());
				}
 				
 				
 			}
 			catch (Exception e) 
 			{ 
 				//finaliza();
 				//reinicia();
 				try { JOptionPane.showMessageDialog(null, "PeerButton: Error while joining peer:\n" + e);  } catch (ExceptionInInitializerError exc) { }
 			}
 			
 		}
 	}
	
	
	
	
	
}
