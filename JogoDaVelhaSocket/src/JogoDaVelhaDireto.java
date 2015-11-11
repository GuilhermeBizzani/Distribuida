/*  
TIC TAC TOE - XO Multiplayer Game
visit: youtube.com/defektruke
by Aleksandar Dj.

Ultimo updates:

10/11 21:58:
Eu baixei


*/ 

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class JogoDaVelhaDireto extends JFrame
{
	private static final long serialVersionUID = 1L;
	
	//ServerSocket, Socket, Input and Output Streams
	private ServerSocket serverSocket = null;
	private Socket konekcija = null;
	private ObjectInputStream ulaz = null;
	private ObjectOutputStream izlaz = null;

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
	
	private JTextArea textArea;										// text area on right side of frame for chat and notifications
	private JScrollPane sp;											// scroll pane for text area
	
	private JTextField ip, port, nick, message; 					// IP address, port number, nickname, chat message
	private JButton join, create, novaPartija; 						// buttons : JOIN, CREATE, NEW GAME
	
	private String polje[] = { "","","", "","","", "","","" }; 		// FIELDS XO (see example in multiline comment)
	/*
	Explanation:
	for ex. if polje = { "X", "X", "X", ...}; then X win
	[X][X][X]
	[ ][ ][ ]
	[ ][ ][ ]
	2nd ex. if polje = { "O","","", "","O","", "","","O" };
	[O][ ][ ]
	[ ][O][ ]
	[ ][ ][O]
	 */
	
	private String xo = ""; 										// server is X , client is O
	private String nick1, nick2, poruka; 							// nick1 server, nick2 client, chat message
	
	private boolean signal; 										// signal for "WHOSE TURN"
	private boolean signalZaSlanje = true;							// if this signal is false, then stop sending messages over Internet
	
	private int brPartije = 0;										//The number of parties // 0=X play first; 1=O play 2nd; 3=X turn....
	private int brPoteze = 0;										//The number of moves, if number is higher or equal to 9 , game is draw
	
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
					posaljiPoruku(xo + "1" + safeSing);
					posaljiPoruku("true" + safeSing);
					signal = false;
					b1.setEnabled(false);
					if(xo.equals("X"))
					{
						polje[0] = "X";
					}
					else
					{
						polje[0] = "O";
					}
					++brPoteze;
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
					posaljiPoruku(xo + "2" + safeSing);
					posaljiPoruku("true" + safeSing);
					signal = false;
					b2.setEnabled(false);
					if(xo.equals("X"))
					{
						polje[1] = "X";
					}
					else
					{
						polje[1] = "O";
					}
					++brPoteze;
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
					posaljiPoruku(xo + "3" + safeSing);
					posaljiPoruku("true" + safeSing);
					signal = false;
					b3.setEnabled(false);
					if(xo.equals("X"))
					{
						polje[2] = "X";
					}
					else
					{
						polje[2] = "O";
					}
					++brPoteze;
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
					posaljiPoruku(xo + "4" + safeSing);
					posaljiPoruku("true" + safeSing);
					signal = false;
					b4.setEnabled(false);
					if(xo.equals("X"))
					{
						polje[3] = "X";
					}
					else
					{
						polje[3] = "O";
					}
					++brPoteze;
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
					posaljiPoruku(xo + "5" + safeSing);
					posaljiPoruku("true" + safeSing);
					signal = false;
					b5.setEnabled(false);
					if(xo.equals("X"))
					{
						polje[4] = "X";
					}
					else
					{
						polje[4] = "O";
					}
					++brPoteze;
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
					posaljiPoruku(xo + "6" + safeSing);
					posaljiPoruku("true" + safeSing);
					signal = false;
					b6.setEnabled(false);
					if(xo.equals("X"))
					{
						polje[5] = "X";
					}
					else
					{
						polje[5] = "O";
					}					
					++brPoteze;
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
					posaljiPoruku(xo + "7" + safeSing);
					posaljiPoruku("true" + safeSing);
					signal = false;
					b7.setEnabled(false);
					if(xo.equals("X"))
					{
						polje[6] = "X";
					}
						
					else
					{
						polje[6] = "O";
					}
					++brPoteze;
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
					posaljiPoruku(xo + "8" + safeSing);
					posaljiPoruku("true" + safeSing);
					signal = false;
					b8.setEnabled(false);
					if(xo.equals("X"))
					{
						polje[7] = "X";
					}
					else
					{
						polje[7] = "O";
					}
					++brPoteze;
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
					posaljiPoruku(xo + "9" + safeSing);
					posaljiPoruku("true" + safeSing);
					signal = false;
					b9.setEnabled(false);
					if(xo.equals("X"))
					{
						polje[8] = "X";
					}
					else
					{
						polje[8] = "O";
					}
					++brPoteze;
					proveriTabelu();
				}	
			}
		});
		pCenter.add(b9);
		
		postaviSve(false); 	// set all buttons on false till we wait for client to join
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
		pLista.setPreferredSize(new Dimension(170, height));
		JTextArea textAreaa = new JTextArea();
		textAreaa.setLineWrap(true);
		textAreaa.setEditable(false);
		textAreaa.setFont(fontButtons);
		textAreaa.append("Game started\n"); //First line of the chat
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
					posaljiPoruku(message.getText());
					message.setText(" ");
				}
			}
		});
		pSouth.add(message, BorderLayout.CENTER);
		add(pSouth, BorderLayout.SOUTH);
		
		ip = new JTextField("127.0.0.1");
		ip.setToolTipText("Enter Host IP addres");
		ip.setPreferredSize(new Dimension(100, 25));
		port = new JTextField("9876");
		port.setToolTipText("Enter Host PORT nubmer, default:9876");
		port.setPreferredSize(new Dimension(100, 25));
		nick = new JTextField();
		nick.setToolTipText("Enter your Nickname");
		nick.setPreferredSize(new Dimension(100, 25));
		
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
					
					konekcija = new Socket(ip.getText(), Integer.parseInt(port.getText())); 
					
					izlaz = new ObjectOutputStream(konekcija.getOutputStream());
					izlaz.flush();
					ulaz = new ObjectInputStream(konekcija.getInputStream());
					
					poruka = (String) ulaz.readObject();
					textArea.append(poruka + "\n");
					scrollToBottom();
					
					xo = "O";
					signal = false;
					
					nick2 = nick.getText();
					
					poruka = (String) ulaz.readObject(); // get nick from host
					nick1 = "" + poruka;
					
					posaljiPoruku(nick2);
					
					postaviSve(true);
					message.setEditable(true);
					
					ip.setEnabled(false);
					port.setEnabled(false);
					nick.setEnabled(false);
					
					textArea.append("X plays first!\n");
					scrollToBottom();
					
					join.setEnabled(false);
					create.setEnabled(false);
					ip.setEnabled(false);
					port.setEnabled(false);
					nick.setEnabled(false);
					
					new PrimajPoruke("PorukaOdServera"); // thread for receive data from host		
				}
				catch(Exception e)
				{
					ugasiSve();
					pokreniSve();
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
				posaljiPoruku("ZahtevZaNovuPartiju!" + safeSing); // send request to client, for new game
				
				++brPartije;
				
				for (int i=0; i<polje.length; i++)
				{
					polje[i] = "";
				}
				
				if(brPartije %2 == 0)
				{
					signal = true;
					textArea.append("X  plays first!\n");
					scrollToBottom();
					posaljiPoruku("false" + safeSing);
					posaljiPoruku("X  plays first!");
				}
				else 
				{
					signal = false;
					posaljiPoruku("true" + safeSing);
					textArea.append("O plays first!\n");
					scrollToBottom();
					posaljiPoruku("O  plays first!");
				}
				
				postaviTekstPolja();
				postaviSve(true);
				novaPartija.setEnabled(false);
			}
		});
		
		JPanel pNorth = new JPanel();
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
					ip.setText(thisIp.getHostAddress());
				} 
				catch (Exception e) 
				{ 
					ip.setText("127.0.0.1"); 
				}
			}	
			
			public void windowClosing(WindowEvent event) 
			{
				if(konekcija != null) 
				{
					posaljiPoruku("Going offline!");
				}
				ugasiSve();
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
				konekcija = serverSocket.accept();
				
				izlaz = new ObjectOutputStream(konekcija.getOutputStream());
				izlaz.flush();
				ulaz = new ObjectInputStream(konekcija.getInputStream());
				posaljiPoruku(nick.getText() +  ": Successfully connected!");
				textArea.append("Client Successfully connected!\n");
				scrollToBottom();
				
				xo = "X";
				signal = true;
				
				nick1 = nick.getText();
				
				posaljiPoruku(nick1);
				
				poruka = (String) ulaz.readObject(); // prima NICK OD SERVERA
				nick2 = "" + poruka;
				
				postaviSve(true);
				message.setEditable(true);
				ip.setEnabled(false);
				
				textArea.append("X plays first!\n");
				scrollToBottom();
				new PrimajPoruke("PorukaOdKlijenta"); 
			}
			catch (Exception e) 
			{ 
				ugasiSve();
				pokreniSve();
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
			(polje[0].equals("X") && polje[1].equals("X") && polje[2].equals("X")) || 
			(polje[3].equals("X") && polje[4].equals("X") && polje[5].equals("X")) ||
			(polje[6].equals("X") && polje[7].equals("X") && polje[8].equals("X")) ||
			// CHECK X POSITIONS - HORIZONTAL
			(polje[0].equals("X") && polje[3].equals("X") && polje[6].equals("X")) ||
			(polje[1].equals("X") && polje[4].equals("X") && polje[7].equals("X")) ||
			(polje[2].equals("X") && polje[5].equals("X") && polje[8].equals("X")) ||
			// CHECK X POSITIONS - DIAGONAL
			(polje[0].equals("X") && polje[4].equals("X") && polje[8].equals("X")) ||
			(polje[2].equals("X") && polje[4].equals("X") && polje[6].equals("X"))
		)
		{
			brPoteze = 0;
			postaviSve(false);
			JOptionPane.showMessageDialog(null, nick1 + " je pobedio! WIN!");
			if(xo.equals("X")) { novaPartija.setEnabled(true); }
		}
		// ------_O_O_O_------
		else if
		(
				// CHECK O POSITIONS - HORIZONTAL
				(polje[0].equals("O") && polje[1].equals("O") && polje[2].equals("O")) ||
				(polje[3].equals("O") && polje[4].equals("O") && polje[5].equals("O")) ||
				(polje[6].equals("O") && polje[7].equals("O") && polje[8].equals("O")) ||
				// CHECK O POSITIONS - VERTICAL
				(polje[0].equals("O") && polje[3].equals("O") && polje[6].equals("O")) ||
				(polje[1].equals("O") && polje[4].equals("O") && polje[7].equals("O")) ||
				(polje[2].equals("O") && polje[5].equals("O") && polje[8].equals("O")) ||
				// CHECK O POSITIONS - DIAGONAL
				(polje[0].equals("O") && polje[4].equals("O") && polje[8].equals("O")) ||
				(polje[2].equals("O") && polje[4].equals("O") && polje[6].equals("O"))
		)
		{
			brPoteze = 0;
			postaviSve(false);
			JOptionPane.showMessageDialog(null, nick2 + " je pobedio! WIN!");
			if(xo.equals("X")) { novaPartija.setEnabled(true); }
		}
		else
		{
			//CHECK IF IS DRAW
			if(brPoteze >= 9)
			{
				brPoteze = 0;
				posaljiPoruku("NERESENO!" + safeSing);
				JOptionPane.showMessageDialog(null, "DRAW/NERESENO!");
				if(xo.equals("X")) { novaPartija.setEnabled(true); }
			}
		}
	}

	// --- enable/disable buttons ---
	private void postaviSve(boolean b)
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
	private void posaljiPoruku(String p)
	{			
		try
		{
			if(signalZaSlanje)
			{
				izlaz.writeObject(p);
				izlaz.flush();
			}
		}
		catch(SocketException e)
		{
			if(signalZaSlanje)
			{
				signalZaSlanje = false;
				ugasiSve();
				pokreniSve();
			}
		}
		catch(Exception e) 
		{ 
			if(signalZaSlanje)
			{
				signalZaSlanje = false;
				ugasiSve();
				pokreniSve();
				try { JOptionPane.showMessageDialog(null, "Sending data/Disconnect:\n" + e); } catch (ExceptionInInitializerError exc) { }
			}
		}
	}
	
	// --- Receive data/messages thread ---
	private class PrimajPoruke implements Runnable
	{	
		private boolean nitSig;
		private String imeNiti;
		
		public PrimajPoruke(String i)
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
					poruka = "";
					poruka = (String) ulaz.readObject();			// receive messages
					
					if(imeNiti.equals("PorukaOdServera")) 			// client receive data from host/server
					{
						if(poruka.equalsIgnoreCase("true" + safeSing))
						{
							signal = true;
						}
						else if(poruka.equalsIgnoreCase("false" + safeSing))
						{
							signal = false;
						}
						else if(poruka.equalsIgnoreCase("NERESENO!" + safeSing))
						{
							JOptionPane.showMessageDialog(null, "DRAW/NERESENO!");
						}
						else if(poruka.equalsIgnoreCase("ZahtevZaNovuPartiju!" + safeSing))
						{
							for (int i=0; i<polje.length; i++)
							{
								polje[i] = "";
							}
							signal = true;
							postaviTekstPolja();
							postaviSve(true);
						}
						else if(poruka.equalsIgnoreCase("X1" + safeSing))
						{
							b1.setText("X");
							polje[0] = "X";
							b1.setEnabled(false);
							proveriTabelu();
						}
						else if(poruka.equalsIgnoreCase("X2" + safeSing))
						{
							b2.setText("X");
							polje[1] = "X";
							b2.setEnabled(false);
							proveriTabelu();
						}
						else if(poruka.equalsIgnoreCase("X3" + safeSing))
						{
							b3.setText("X");
							polje[2] = "X";
							b3.setEnabled(false);
							proveriTabelu();
						}
						else if(poruka.equalsIgnoreCase("X4" + safeSing))
						{
							b4.setText("X");
							polje[3] = "X";
							b4.setEnabled(false);
							proveriTabelu();
						}
						else if(poruka.equalsIgnoreCase("X5" + safeSing))
						{
							b5.setText("X");
							polje[4] = "X";
							b5.setEnabled(false);
							proveriTabelu();
						}
						else if(poruka.equalsIgnoreCase("X6" + safeSing))
						{
							b6.setText("X");
							polje[5] = "X";
							b6.setEnabled(false);
							proveriTabelu();
						}
						else if(poruka.equalsIgnoreCase("X7" + safeSing))
						{
							b7.setText("X");
							polje[6] = "X";
							b7.setEnabled(false);
							proveriTabelu();
						}
						else if(poruka.equalsIgnoreCase("X8" + safeSing))
						{
							b8.setText("X");
							polje[7] = "X";
							b8.setEnabled(false);
							proveriTabelu();
						}
						else if(poruka.equalsIgnoreCase("X9" + safeSing))
						{
							b9.setText("X");
							polje[8] = "X";
							b9.setEnabled(false);
							proveriTabelu();
						}
						else
						{
							if(poruka.endsWith(safeSing))
							{
								poruka = poruka.substring(0, poruka.length() - safeSing.length());
							}
							textArea.append(nick1 + ":" + poruka + "\n");
							scrollToBottom();
						}
					}
					else if(imeNiti.equals("PorukaOdKlijenta"))			// host/server receive data from client
					{
						if(poruka.equalsIgnoreCase("true" + safeSing))
						{
							signal = true;
						}
						else if(poruka.equalsIgnoreCase("O1" + safeSing))
						{
							++brPoteze;
							b1.setText("O");
							polje[0] = "O";
							b1.setEnabled(false);
							proveriTabelu();
						}
						else if(poruka.equalsIgnoreCase("O2" + safeSing))
						{
							++brPoteze;
							b2.setText("O");
							polje[1] = "O";
							b2.setEnabled(false);
							proveriTabelu();
						}
						else if(poruka.equalsIgnoreCase("O3" + safeSing))
						{
							++brPoteze;
							b3.setText("O");
							polje[2] = "O";
							b3.setEnabled(false);
							proveriTabelu();
						}
						else if(poruka.equalsIgnoreCase("O4" + safeSing))
						{
							++brPoteze;
							b4.setText("O");
							polje[3] = "O";
							b4.setEnabled(false);
							proveriTabelu();
						}
						else if(poruka.equalsIgnoreCase("O5" + safeSing))
						{
							++brPoteze;
							b5.setText("O");
							polje[4] = "O";
							b5.setEnabled(false);
							proveriTabelu();
						}
						else if(poruka.equalsIgnoreCase("O6" + safeSing))
						{
							++brPoteze;
							b6.setText("O");
							polje[5] = "O";
							b6.setEnabled(false);
							proveriTabelu();
						}
						else if(poruka.equalsIgnoreCase("O7" + safeSing))
						{
							++brPoteze;
							b7.setText("O");
							polje[6] = "O";
							b7.setEnabled(false);
							proveriTabelu();
						}
						else if(poruka.equalsIgnoreCase("O8" + safeSing))
						{
							++brPoteze;
							b8.setText("O");
							polje[7] = "O";
							b8.setEnabled(false);
							proveriTabelu();
						}
						else if(poruka.equalsIgnoreCase("O9" + safeSing))
						{
							++brPoteze;
							b9.setText("O");
							polje[8] = "O";
							b9.setEnabled(false);
							proveriTabelu();
						}
						else
						{
							if(poruka.endsWith(safeSing))
							{
								poruka = poruka.substring(0, poruka.length() - safeSing.length());
							}
							textArea.append(nick2 + ":" + poruka + "\n");
							scrollToBottom();
						}
					}
				}
				catch (Exception e)
				{
					nitSig = false;
					ugasiSve();
					pokreniSve();
					try { JOptionPane.showMessageDialog(null, "Receiving Data Failed/Disconnect:\n" + e); } catch (ExceptionInInitializerError exc) { }
				}
			}
		}
	}
	
	// --- restart the game to the initial state ---
	private void pokreniSve()
	{
		postaviTekstPolja();
		postaviSve(false);
		
		poruka = "";
		xo = "";
		signalZaSlanje = true;
		brPoteze = 0;
		brPartije = 0;
		
		for (int i=0; i<polje.length; i++)
		{
			polje[i] = "";
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
	private void ugasiSve()
	{
		try { izlaz.flush(); 		} catch (Exception e) { }
		try { izlaz.close(); 		} catch (Exception e) { }
		try { ulaz.close(); 		} catch (Exception e) { }
		try { serverSocket.close();	} catch (Exception e) { }
		try { konekcija.close(); 	} catch (Exception e) { }
	}
	
	// --- scroll to bottom when receive chat message ---
	public void scrollToBottom()
	{
		textArea.setCaretPosition(textArea.getText().length());
	}

	// --- Main ---
	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				new JogoDaVelhaDireto().setVisible(true);				
			}
		});
	}
}
