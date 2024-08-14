import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class NorthwestCornerMethodApp extends JFrame {
    private static final int TABLE_ROWS = 20;
    private static final int TABLE_COLS = 20;
    private static final int CELL_WIDTH = 80;
    private static final int CELL_HEIGHT = 80;
    private final ArrayList<PointWithName> points = new ArrayList<>();
    private final List<PointWithName> shortestPath = new ArrayList<>();
    private JComboBox<String> fromComboBox;
    private JComboBox<String> toComboBox;
    private Image mapImage;

    public NorthwestCornerMethodApp() {
        setTitle("Northwest Corner Method");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(CELL_WIDTH * TABLE_COLS, CELL_HEIGHT * TABLE_ROWS + 50)); // Added space for text fields
        initializePoints(); // Initialize points before setting up UI
        setupUI();
        loadMapImage(); // Load the map image
        pack();
        setLocationRelativeTo(null);
    }

    private void loadMapImage() {
        try {
            File mapFile = new File("map.png"); // Replace with the actual path to your map image file
            mapImage = ImageIO.read(mapFile);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading map image: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void drawMap(Graphics g) {
        if (mapImage != null) {
            double widthScale = (double) getWidth() / mapImage.getWidth(null);
            double heightScale = (double) getHeight() / mapImage.getHeight(null);
            double scale = Math.min(widthScale, heightScale);

            int newWidth = (int) (mapImage.getWidth(null) * scale);
            int newHeight = (int) (mapImage.getHeight(null) * scale);

            int x = (getWidth() - newWidth) / 2;
            int y = (getHeight() - newHeight) / 2;

            g.drawImage(mapImage, x, y, newWidth, newHeight, this);
        }
    }

    private void drawPointsOnMap(Graphics g) {
        if (mapImage != null) {
            for (PointWithName point : points) {
                int x = point.y * CELL_WIDTH + CELL_WIDTH / 2;
                int y = point.x * CELL_HEIGHT + CELL_HEIGHT / 2;
                g.setColor(Color.BLUE);
                g.fillOval(x - 5, y - 5, 10, 10);
                g.drawString(point.name, x - 10, y - 10);
            }
        }
    }

    private void setupUI() {
        JPanel drawingPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawMap(g); // Draw the map as the background
                drawTable(g);
                drawPointsOnMap(g); // Draw points on the map
                drawShortestPath(g);
            }
        };
        add(drawingPanel);

        JPanel controlPanel = new JPanel();
        fromComboBox = new JComboBox<>();
        toComboBox = new JComboBox<>();

        for (PointWithName point : points) {
            fromComboBox.addItem(point.name);
            toComboBox.addItem(point.name);
        }

        JButton connectButton = new JButton("Connect");
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String fromName = fromComboBox.getSelectedItem().toString();
                String toName = toComboBox.getSelectedItem().toString();

                PointWithName fromPoint = findPointByName(fromName);
                PointWithName toPoint = findPointByName(toName);

                if (fromPoint != null && toPoint != null) {
                    shortestPath.clear();
                    shortestPath.add(fromPoint);
                    shortestPath.add(toPoint);
                    repaint();
                } else {
                    JOptionPane.showMessageDialog(NorthwestCornerMethodApp.this, "Invalid points!");
                }
            }
        });

        controlPanel.add(new JLabel("From: "));
        controlPanel.add(fromComboBox);
        controlPanel.add(new JLabel("To: "));
        controlPanel.add(toComboBox);
        controlPanel.add(connectButton);
        add(controlPanel, BorderLayout.NORTH);
    }

    private void initializePoints() {
        points.add(new PointWithName(13, 14, "Senate House"));
        points.add(new PointWithName(11, 5, "Central Administration"));
        points.add(new PointWithName(12, 12, "John Evans Atta Mills Library"));
        points.add(new PointWithName(14, 10, "Kwame Nkrumah Hall"));
        points.add(new PointWithName(0, 6, "Achimota Hall"));
        points.add(new PointWithName(5, 11, "Commonwealth Hall"));
        points.add(new PointWithName(1, 1, "Legon Hall"));
        points.add(new PointWithName(7, 13, "Balfour Hall"));
        points.add(new PointWithName(8, 8, "Herbert Macaulay Hall"));
        points.add(new PointWithName(10, 4, "Nana Oforiatta Boateng Hall"));
        points.add(new PointWithName(9, 9, "Volta Hall"));
        points.add(new PointWithName(4, 7, "Tawiah Hall"));
        points.add(new PointWithName(2, 2, "Legon School of Business"));
        points.add(new PointWithName(3, 3, "School of Engineering"));
        points.add(new PointWithName(6, 6, "School of Law"));
        points.add(new PointWithName(15, 0, "Nobel House"));
    }

    private void drawTable(Graphics g) {
        for (int row = 0; row < TABLE_ROWS; row++) {
            for (int col = 0; col < TABLE_COLS; col++) {
                int x = col * CELL_WIDTH;
                int y = row * CELL_HEIGHT;
                g.drawRect(x, y, CELL_WIDTH, CELL_HEIGHT);
            }
        }
    }

    private double[] calculateDistanceAndTime(PointWithName p1, PointWithName p2, double speed) {
        final int R = 6371; // Earth's radius in kilometers
        double lat1 = Math.toRadians(p1.x);
        double lon1 = Math.toRadians(p1.y);
        double lat2 = Math.toRadians(p2.x);
        double lon2 = Math.toRadians(p2.y);

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;

        // Calculate time (hours) based on distance and speed
        double time = distance / speed;

        return new double[]{distance, time};
    }

    private List<Point> generatePointsOnEdge(Point p1, Point p2) {
        List<Point> pointsOnEdge = new ArrayList<>();
        int x1 = p1.x;
        int y1 = p1.y;
        int x2 = p2.x;
        int y2 = p2.y;

        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);

        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;

        int err = dx - dy;

        while (true) {
            pointsOnEdge.add(new Point(x1, y1));

            if (x1 == x2 && y1 == y2) {
                break;
            }

            int e2 = 2 * err;

            if (e2 > -dy) {
                err -= dy;
                x1 += sx;
            }

            if (e2 < dx) {
                err += dx;
                y1 += sy;
            }
        }

        return pointsOnEdge;
    }

    private PointWithName findPointByName(String name) {
        for (PointWithName point : points) {
            if (point.name.equals(name)) {
                return point;
            }
        }
        return null;
    }

    private void drawShortestPath(Graphics g) {
        if (!shortestPath.isEmpty()) {
            g.setColor(Color.BLUE);
            PointWithName prevPoint = null;
            for (PointWithName point : shortestPath) {
                if (prevPoint != null) {
                    List<Point> pointsOnEdge = generatePointsOnEdge(
                            new Point(prevPoint.y, prevPoint.x),
                            new Point(point.y, point.x)
                    );
                    for (Point p : pointsOnEdge) {
                        int x = p.x * CELL_WIDTH + CELL_WIDTH / 2;
                        int y = p.y * CELL_HEIGHT + CELL_HEIGHT / 2;
                        g.fillOval(x - 3, y - 3, 6, 6);
                    }
                }
                prevPoint = point;
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new NorthwestCornerMethodApp().setVisible(true);
            }
        });
    }

    public ArrayList<PointWithName> getPoints() {
        return points;
    }

    private static class PointWithName extends Point {
        String name;

        PointWithName(int x, int y, String name) {
            super(x, y);
            this.name = name;
        }
    }
}