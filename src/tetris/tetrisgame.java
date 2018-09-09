package tetris;

import java.util.*;
import java.awt.event.*;
import java.awt.*;

class Square
{
	int x, y, c;

	Square(int x, int y, int c) // �ǽ������� �ּҴ����� �簢�� ����
	{
		this.x = x;
		this.y = y;
		this.c = c;
	}

	boolean InBounds() // ��谪 üũ
	{
		return (x >= 0 && x < tetrisgame.cols && y >= 0 && y < tetrisgame.rows);
	}

	boolean IsEqual(Square s) // �׿��ִ� �ǽ��� ��谪 üũ
	{
		return x == s.x && y == s.y && c == s.c;
	}
}

public class tetrisgame extends Frame implements Runnable, ActionListener
{
	MenuBar mb;
	Menu game, help;
	MenuItem start, end, helpItem;
	Image logo;

	Dialog di, helpDi;
	Label diLabel1, diLabel2;
	TextField field;
	String name;
	Button btn;

	static int sqlength = 17; // �ǽ� �ּҴ��� ������ ũ��
	static final int xoffset = 120; // ���������� ä������ ����� ������
	static int cols = 10; // ����� ���ι���
	static int rows = 20; // ����� ���ι���
	int f[][];
	int what = (int) (Math.random() * 7); // �ǽ��� ����� ����� ��
	int what2; // �̸����� �ǽ��� ����� ��
	static Square curpiece[] = new Square[4];
	static Square newpiece[] = new Square[4];
	boolean lost;
	boolean neednewpiece = true;
	Thread killme = null;
	Color colors[];
	int curval, score = 0; // �ǽ��� ������ ������ �ʱ�ȭ
	int level;
	int removeline;

	public tetrisgame()
	{
		super("��Ʈ��������");
		Toolkit tk = Toolkit.getDefaultToolkit();
		logo = tk.getImage("C:/Users/hwi/Desktop/��/mark.png");
		setIconImage(logo);
		setLayout(null);

		mb = new MenuBar();
		setMenuBar(mb);

		game = new Menu("����");
		mb.add(game);

		help = new Menu("help");
		mb.setHelpMenu(help);

		start = new MenuItem("����");
		start.addActionListener(this);
		game.add(start);

		end = new MenuItem("����");
		end.addActionListener(this);
		game.add(end);

		helpItem = new MenuItem("����");
		helpItem.addActionListener(this);
		help.add(helpItem);

		helpDi = new Dialog(this, "help", true);
		helpDi.setLayout(new FlowLayout());
		helpDi.addWindowListener(new WinDiaHandler());
		helpDi.resize(400, 100);

		diLabel1 = new Label("����Ű (�� ������");
		helpDi.add(diLabel1);
		diLabel2 = new Label("������ �������� 10~20���̸� ������ ���� ���ٶ����� 50 ���� �߰��ȴ�");
		helpDi.add(diLabel2);

		addKeyListener(new KeyHandler());
		addWindowListener(new WindowHandler());

		setVisible(true);

		f = new int[cols][rows + 4];

		this.setSize(xoffset + sqlength * cols + 30, sqlength * (rows + 4));

		colors = new Color[8];
		colors[0] = new Color(40, 40, 40);

		colors[1] = new Color(255, 0, 0);
		colors[2] = new Color(0, 255, 0);
		colors[3] = new Color(0, 200, 255);
		colors[4] = new Color(255, 255, 0);
		colors[5] = new Color(255, 150, 0);
		colors[6] = new Color(210, 0, 240);
		colors[7] = new Color(40, 0, 240);

	}

	public void start()
	{
		for (int i = 0; i < cols; i++)
		{
			for (int j = 0; j < rows + 4; j++)
			{
				f[i][j] = 0; // �ʱ����� ���������� ����
			}
		}

		level = 3; // �� �����ʱ�ȭ
		score = 0;
		removeline = 0;
		curval = -1;
		neednewpiece = true;
		lost = false;
		repaint();
		(killme = new Thread(this)).start(); // ������ ȣ��
		requestFocus(); // ���콺 ��Ŀ�� ��û�Ͽ� ���ۻ��¸� �˸�
	}

	public synchronized void stop()
	{
		if (killme != null)
			killme.stop();
		killme = null;
	}

	public void run()
	{
		while (!lost)
		{
			int tim = level;
			try
			{
				if (neednewpiece)
				{
					tim = 10;
					Thread.sleep(tim);
				}
				Thread.sleep(1000 / tim); // �����带 �̿��� �ӵ� ����
			} catch (InterruptedException e)
			{
			}
			
			if (neednewpiece)
			{
				if (curval > 0)
				{
					level = 3 + removeline / 10; // ���� ��� ����(�����Ǵ� ���α���)
				}
				removelines();
				newpiece();
				score += curval;
				what = (int) (Math.random() * 7);
				neednewpiece = false;
			} else
			{
				neednewpiece = !movecurpiece(0, -1, false); // ������ �����带 �Ʒ��� �̵�
			}
			repaint();
		}
		killme = null; // ������ ������ ����

		di = new Dialog(this, "�����մϴ�.", true); // ������� ���̾�α� ����
		di.setLayout(new FlowLayout());
		di.add(new Label("�̸�:"));
		field = new TextField("", 10);
		field.addActionListener(this);
		btn = new Button("���");
		btn.addActionListener(this);
		di.add(field);
		di.add(new Label(Integer.toString(score)));
		di.add(btn);
		di.addWindowListener(new WinDiaHandler());
		di.resize(300, 100);
		di.show();
	}

	private void newpiece() // ���ǽ� ���� �κ�
	{
		Square old[] = new Square[4]; // �ǽ��� ������ ���� ����Ʈ ���� ����
		old[0] = old[1] = old[2] = old[3] = new Square(-1, -1, 0);
		int m = cols / 2;

		what2 = what; // �̸����⿡�� ������ �ǽ��� ���ӿ� �̿�Ǵ� �ǽ��� ����
		switch (what2) {
			case 0:
				// �� �� m ��
				curval = 10; // �ǽ��� ����
				curpiece[0] = new Square(m, rows - 1, 1); // 0�� �迭�� ���� ����
				curpiece[1] = new Square(m - 1, rows - 1, 1);
				curpiece[2] = new Square(m + 1, rows - 1, 1);
				curpiece[3] = new Square(m + 2, rows - 1, 1);
				break;
			case 1:
				// �� m ��
				//   m
				curval = 15;
				curpiece[0] = new Square(m, rows - 1, 2);
				curpiece[1] = new Square(m - 1, rows - 1, 2);
				curpiece[2] = new Square(m, rows - 2, 2);
				curpiece[3] = new Square(m + 1, rows - 1, 2);
				break;
			case 2:
				//   m ��
				// �� m
				curval = 20;
				curpiece[0] = new Square(m, rows - 2, 3);
				curpiece[1] = new Square(m - 1, rows - 2, 3);
				curpiece[2] = new Square(m, rows - 1, 3);
				curpiece[3] = new Square(m + 1, rows - 1, 3);
				break;
			case 3:
				// �� m
				// �� m
				curval = 20;
				curpiece[0] = new Square(m, rows - 2, 4);
				curpiece[1] = new Square(m + 1, rows - 2, 4);
				curpiece[2] = new Square(m - 1, rows - 1, 4);
				curpiece[3] = new Square(m, rows - 1, 4);
				break;
			case 4:
				// ��m
				// ��m
				curval = 10;
				curpiece[0] = new Square(m - 1, rows - 2, 5);
				curpiece[1] = new Square(m, rows - 2, 5);
				curpiece[2] = new Square(m - 1, rows - 1, 5);
				curpiece[3] = new Square(m, rows - 1, 5);
				break;
			case 5:
				// ��
				// ��m��
				curval = 15;
				curpiece[0] = new Square(m, rows - 2, 6);
				curpiece[1] = new Square(m - 1, rows - 2, 6);
				curpiece[2] = new Square(m + 1, rows - 2, 6);
				curpiece[3] = new Square(m + 1, rows - 1, 6);
				break;
			case 6:
				// ��
				// ��m��
				curval = 15;
				curpiece[0] = new Square(m, rows - 2, 7);
				curpiece[1] = new Square(m - 1, rows - 2, 7);
				curpiece[2] = new Square(m + 1, rows - 2, 7);
				curpiece[3] = new Square(m - 1, rows - 1, 7);
				break;
		}
		lost = !movepiece(old, curpiece); // �ǽ��� �ٽ׿��� ������ �� ������ ������ ����ǰ� ��
	}

	public synchronized void paint(Graphics g)
	{
		try
		{
			g.setFont(new java.awt.Font("impact", 10, 16)); // ������ ���¸� �����ִ� ���ڵ���
															// �����ϴ� �κ�
			int gx = sqlength;
			int gy = 100;

			g.clearRect(gx, gy - 25, xoffset - 20, 200); // ���������� �����ϱ� ���ؼ� �����ִ�
															// �κ�

			g.drawString("Score: " + score, gx, gy);
			g.drawString("Removeline: " + removeline, gx, gy + 30);
			g.drawString("Level: " + level, gx, gy + 60);
			g.drawString("Next: ", gx, gy + 90);

			switch (what) // �̸����� �ǽ�
			{
				case 0:
					// ����m��
					g.setColor(colors[1]);
					g.fillRect(gx + sqlength * 0, gy + 120 + sqlength * 1, sqlength, sqlength);
					g.fillRect(gx + sqlength * 1, gy + 120 + sqlength * 1, sqlength, sqlength);
					g.fillRect(gx + sqlength * 2, gy + 120 + sqlength * 1, sqlength, sqlength);
					g.fillRect(gx + sqlength * 3, gy + 120 + sqlength * 1, sqlength, sqlength);

					break;
				case 1:
					// ��m��4
					// m
					g.setColor(colors[2]);
					g.fillRect(gx + sqlength * 0, gy + 120 + sqlength * 0, sqlength, sqlength);
					g.fillRect(gx + sqlength * 1, gy + 120 + sqlength * 0, sqlength, sqlength);
					g.fillRect(gx + sqlength * 2, gy + 120 + sqlength * 0, sqlength, sqlength);
					g.fillRect(gx + sqlength * 1, gy + 120 + sqlength * 1, sqlength, sqlength);
					break;
				case 2:
					// m��
					// ��m
					g.setColor(colors[3]);
					g.fillRect(gx + sqlength * 0, gy + 120 + sqlength * 1, sqlength, sqlength);
					g.fillRect(gx + sqlength * 1, gy + 120 + sqlength * 1, sqlength, sqlength);
					g.fillRect(gx + sqlength * 1, gy + 120 + sqlength * 0, sqlength, sqlength);
					g.fillRect(gx + sqlength * 2, gy + 120 + sqlength * 0, sqlength, sqlength);
					break;
				case 3:
					// ��m
					// m��
					g.setColor(colors[4]);
					g.fillRect(gx + sqlength * 2, gy + 120 + sqlength * 1, sqlength, sqlength);
					g.fillRect(gx + sqlength * 1, gy + 120 + sqlength * 1, sqlength, sqlength);
					g.fillRect(gx + sqlength * 1, gy + 120 + sqlength * 0, sqlength, sqlength);
					g.fillRect(gx + sqlength * 0, gy + 120 + sqlength * 0, sqlength, sqlength);
					break;
				case 4:
					// m��
					// m��
					g.setColor(colors[5]);
					g.fillRect(gx + sqlength * 0, gy + 120 + sqlength * 0, sqlength, sqlength);
					g.fillRect(gx + sqlength * 0, gy + 120 + sqlength * 1, sqlength, sqlength);
					g.fillRect(gx + sqlength * 1, gy + 120 + sqlength * 0, sqlength, sqlength);
					g.fillRect(gx + sqlength * 1, gy + 120 + sqlength * 1, sqlength, sqlength);
					break;
				case 5:
					// ��
					// ��m��
					g.setColor(colors[6]);
					g.fillRect(gx + sqlength * 2, gy + 120 + sqlength * 0, sqlength, sqlength);
					g.fillRect(gx + sqlength * 0, gy + 120 + sqlength * 1, sqlength, sqlength);
					g.fillRect(gx + sqlength * 1, gy + 120 + sqlength * 1, sqlength, sqlength);
					g.fillRect(gx + sqlength * 2, gy + 120 + sqlength * 1, sqlength, sqlength);
					break;
				case 6:
					// ��
					// ��m��
					g.setColor(colors[7]);
					g.fillRect(gx + sqlength * 0, gy + 120 + sqlength * 0, sqlength, sqlength);
					g.fillRect(gx + sqlength * 0, gy + 120 + sqlength * 1, sqlength, sqlength);
					g.fillRect(gx + sqlength * 1, gy + 120 + sqlength * 1, sqlength, sqlength);
					g.fillRect(gx + sqlength * 2, gy + 120 + sqlength * 1, sqlength, sqlength);
					break;
			}

			for (int i = 0; i < cols; i++) // ��濡 �ǽ������ ���� ������
			{
				for (int j = 0; j < rows; j++)
				{
					g.setColor(colors[f[i][rows - 1 - j]]);
					g.fillRect(xoffset + sqlength * i, 3 * sqlength + sqlength * j, sqlength, sqlength);
				}
			}
		} catch (Exception e)
		{
			repaint();
		}
	}

	public synchronized void update(Graphics g)
	{
		paint(g);
	}

	private synchronized boolean movecurpiece(int byx, int byy, boolean rotate)
	{
		Square newpos[] = new Square[4];

		for (int i = 0; i < 4; i++)
		{
			if (rotate) // �ǽ��� ȸ�� �κ�
			{
				if (what2 != 0)
				{
					int dx = curpiece[i].x - curpiece[0].x;
					int dy = curpiece[i].y - curpiece[0].y;
					newpos[i] = new Square(curpiece[0].x - dy, curpiece[0].y + dx, curpiece[i].c);
				} else
				{
					int dx = curpiece[i].x - curpiece[0].x;
					int dy = curpiece[i].y - curpiece[0].y;
					newpos[i] = new Square(curpiece[0].x + dy, curpiece[0].y - dx, curpiece[i].c);
				}
			} else // ȸ������ �ٷ� �Ʒ��� �̵�
			{
				newpos[i] = new Square(curpiece[i].x + byx, curpiece[i].y + byy, curpiece[i].c);
			}
		}

		if (!movepiece(curpiece, newpos)) // ���� �ǽ��� ���� �ǽ��� �̵� ���ɼ��� üũ
			return false;

		curpiece = newpos; // ���� �ǽ��� �̵��� �ǽ��� ����
		return true;
	}

	private boolean movepiece(Square from[], Square to[])
	{
		outerlabel: 
		for (int i = 0; i < to.length; i++)
		{
			if (!to[i].InBounds()) // ��谪 üũ
			{
				return false;
			}

			if (f[to[i].x][to[i].y] != 0) // ���� ��ϰ� ��ġ�°��� üũ
			{
				for (int j = 0; j < from.length; j++)
				{
					if (to[i].IsEqual(from[j]))
					{
						continue outerlabel;
					}
				}
				return false;
			}
		}

		for (int i = 0; i < from.length; i++) // �̵����� ����� ����
		{
			if (from[i].InBounds())
			{
				f[from[i].x][from[i].y] = 0;
			}
		}

		for (int i = 0; i < to.length; i++) // �̵��� ����� ���� �ʵ忡 ä��
		{
			f[to[i].x][to[i].y] = to[i].c;
		}
		return true;
	}

	private void removelines()
	{
		outerlabel: for (int j = 0; j < rows; j++)
		{
			for (int i = 0; i < cols; i++)
			{
				if (f[i][j] == 0) // �׿��� ����߿� ����� �κ��� ������
				{
					continue outerlabel;
				}
			}
			for (int k = j; k < rows - 1; k++) // �׿��� ��Ͽ��� ������ �� ä���� ���� ���
			{
				for (int i = 0; i < cols; i++)
				{
					f[i][k] = f[i][k + 1];
				}
			}
			j--; // �������� ���� ����
			removeline++;
			score += 50;
		}
	}

	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == end) // �޴��� ������ ���� ����
		{
			setVisible(false);
			dispose();
			System.exit(0);
		} else if (e.getSource() == start) // �޴��� ������ ���� ����
		{
			start();
		} else if ((e.getSource() == btn) || (e.getSource() == field)) // ���̾�α�â����
																		// ��ư Ŭ��
																		// �� ����Ű
																		// �Է�
		{
			di.hide();
			name = field.getText();
			// FileMake.fileWrite(name, score);
			// FileMake.fileRead();
		} else if (e.getSource() == helpItem) // �޴����� ������ �����ؼ� �����ִ� �κ�
		{
			helpDi.show();
		}
	}

	class KeyHandler extends KeyAdapter
	{
		public void keyPressed(KeyEvent e)
		{
			switch (e.getKeyCode()) {
				case 37: // <-
					movecurpiece(-1, 0, false); // x ��ǥ�� �������� -1 ��ŭ ������.
					neednewpiece = false; // �� �ǽ��� �ʿ����,
					repaint(); // update ��û
					break;

				case 39: // ->
					movecurpiece(1, 0, false); // x ��ǥ�� ���������� +1 �̵�
					neednewpiece = false;
					repaint();
					break;

				case 38: // rotate
					if (!neednewpiece && what2 != 4)
					{ // ����ǥ�� ������ ���¿��� rotate
						movecurpiece(0, 0, true);
						repaint();
						neednewpiece = false;
					}
					break;
				case 32: // �����̽���
				case 40: // ��
					while (movecurpiece(0, -1, false))
						;
					repaint();
					break;
			}
		}
	}

	class WindowHandler extends WindowAdapter
	{
		public void windowClosing(WindowEvent e)
		{
			Window w = e.getWindow();
			w.setVisible(false);
			w.dispose();
			System.exit(0);
		}
	}

	class WinDiaHandler extends WindowAdapter
	{
		public void windowClosing(WindowEvent e)
		{
			Window w = e.getWindow();
			w.setVisible(false);
			w.dispose();
		}
	}

	public static void main(String[] args)
	{
		new tetrisgame();
	}

}
