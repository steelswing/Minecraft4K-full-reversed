
import java.awt.Event;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.JFrame;

/**
 * File: Minecraft4K.java
 * Created on 30.12.2023, 0:45:53
 *
 * @author LWJGL2
 */
public class Minecraft4K extends MouseAdapter implements KeyListener {

    private int[] M = new int[32767];
    private JFrame frame;

    public Minecraft4K() {
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(856, 480);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.createBufferStrategy(1);
        frame.addMouseListener(this);
        frame.addMouseMotionListener(this);
        frame.addKeyListener(this);
        run();
    }

    public static BufferedImage createImageFromColorArray(int[] colorArray, int width, int height) {
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int[] pixelData = ((DataBufferInt) bufferedImage.getRaster().getDataBuffer()).getData();
        System.arraycopy(colorArray, 0, pixelData, 0, colorArray.length);
        return bufferedImage;
    }

    public void run() {
        try {
            Random random = new Random(0x1172981L);
            BufferedImage localBufferedImage = new BufferedImage(214, 120, 1);
            int[] pixelData = ((DataBufferInt) localBufferedImage.getRaster().getDataBuffer()).getData();
            int[] blockData = new int[64 * 64 * 64];

            for (int i = 0; i < blockData.length; ++i) {
                blockData[i] = i / 64 % 64 > 32 + random.nextInt(8) ? random.nextInt(8) + 1 : 0;
            }

            final int[] colorArray = new int[16 * 16 * 16 * 3];

            final BufferedImage atlasDump = new BufferedImage(16, 16 * 16, BufferedImage.TYPE_INT_RGB);
            for (int layer = 1; layer < 16; ++layer) {
                int baseBrightness = 255 - random.nextInt(96);

                for (int row = 0; row < 16 * 3; ++row) {
                    for (int col = 0; col < 16; ++col) {
                        int baseColor = 0x966c4a;  // Default color

                        // Color adjustment depending on the layer
                        if (layer == 4) {
                            baseColor = 0x7f7f7f;
                        }

                        // Brightness correction depending on the conditions
                        if (layer != 4 || random.nextInt(3) == 0) {
                            baseBrightness = 255 - random.nextInt(96);
                        }

                        // Special conditions for layer 1
                        if (layer == 1) {
                            int threshold = (col * col * 3 + col * 81 >> 2 & 3) + 18;
                            if (row < threshold) {
                                baseColor = 0x6aaa40;  // Green color
                            } else if (row < threshold + 1) {
                                baseBrightness = baseBrightness * 2 / 3;
                            }
                        }

                        // Conditions for layer 7
                        if (layer == 7) {
                            baseColor = 0x675231;  // Default color
                            if (col > 0 && col < 15 && (row > 0 && row < 15 || row > 32 && row < 47)) {
                                baseColor = 0xbc9862; // Special color
                                int deltaX = col - 7;
                                int deltaY = (row & 15) - 7;
                                int distance = Math.max(Math.abs(deltaX), Math.abs(deltaY));
                                baseBrightness = 196 - random.nextInt(32) + distance % 3 * 32;
                            } else if (random.nextInt(2) == 0) {
                                baseBrightness = baseBrightness * (150 - (col & 1) * 100) / 100;
                            }
                        }

                        // Conditions for layer 5
                        if (layer == 5) {
                            baseColor = 0xb53a15;// Default color
                            if ((col + row / 4 * 4) % 8 == 0 || row % 4 == 0) {
                                baseColor = 0xbcafa5; // Special color
                            }
                        }

                        // Brightness correction for the lower half in layer 8
                        int adjustedBrightness = (row >= 32) ? baseBrightness / 2 : baseBrightness;

                        // Conditions for layer 8
                        if (layer == 8) {
                            baseColor = 0x50d937;// Default color
                            if (random.nextInt(2) == 0) {
                                baseColor = 0;  //Black color
                                adjustedBrightness = 255;
                            }
                        }

                        // Combining color and brightness components
                        int finalColor = (baseColor >> 16 & 0xFF) * adjustedBrightness / 255 << 16 |
                                (baseColor >> 8 & 0xFF) * adjustedBrightness / 255 << 8 |
                                (baseColor & 0xFF) * adjustedBrightness / 255;

                        int subImageIndex = layer * 256 * 3;
                        int pixelIndex = col + row * 16;
                        int channel = row / 16;
                        colorArray[pixelIndex + subImageIndex] = finalColor;

                        if (channel == 0) {
                            atlasDump.setRGB(col, row + (layer * 16), finalColor);
                        }
                    }
                }

            }
            ImageIO.write(atlasDump, "PNG", new File("atlas_dump.png"));

            float cameraAngleX = 96.5F;
            float cameraAngleY = 65.0F;
            float cameraAngleZ = 96.5F;
            float rotationX = 0.0F;
            float rotationY = 0.0F;
            float rotationZ = 0.0F;
            long currentTime = System.currentTimeMillis();
            int selectedBlockIndex = -1;
            int selectedBlockType = 0;
            float rotationAngleX = 0.0F;
            float rotationAngleY = 0.0F;

            while (true) {
                float sinRotationX = (float) Math.sin(rotationAngleX);
                float cosRotationX = (float) Math.cos(rotationAngleX);
                float sinRotationY = (float) Math.sin(rotationAngleY);
                float cosRotationY = (float) Math.cos(rotationAngleY);

                label266:
                while (System.currentTimeMillis() - currentTime > 10L) {
                    if (M[2] > 0) {
                        float mouseXDelta = (M[2] - 428) / 214.0F * 2.0F;
                        float mouseYDelta = (M[3] - 240) / 120.0F * 2.0F;
                        float distance = (float) Math.sqrt((mouseXDelta * mouseXDelta + mouseYDelta * mouseYDelta)) - 1.2F;
                        if (distance < 0.0F) {
                            distance = 0.0F;
                        }

                        if (distance > 0.0F) {
                            rotationAngleX += mouseXDelta * distance / 400.0F;
                            rotationAngleY -= mouseYDelta * distance / 400.0F;
                            if (rotationAngleY < -1.57F) {
                                rotationAngleY = -1.57F;
                            }

                            if (rotationAngleY > 1.57F) {
                                rotationAngleY = 1.57F;
                            }
                        }
                    }

                    currentTime += 10L;
                    float moveX = 0.0F;
                    float moveY = 0.0F;
                    moveY += (M[119] - M[115]) * 0.02F;
                    moveX += (M[100] - M[97]) * 0.02F;
                    rotationX *= 0.5F;
                    rotationY *= 0.99F;
                    rotationZ *= 0.5F;
                    rotationX += sinRotationX * moveY + cosRotationX * moveX;
                    rotationZ += cosRotationX * moveY - sinRotationX * moveX;
                    rotationY += 0.003F;

                    for (int i = 0; i < 3; ++i) {
                        float adjustedAngleX = cameraAngleX + rotationX * ((i + 0) % 3 / 2);
                        float adjustedAngleY = cameraAngleY + rotationY * ((i + 1) % 3 / 2);
                        float adjustedAngleZ = cameraAngleZ + rotationZ * ((i + 2) % 3 / 2);

                        for (int j = 0; j < 12; ++j) {
                            int offsetX = (int) (adjustedAngleX + (j >> 0 & 1) * 0.6F - 0.3F) - 64;
                            int offsetY = (int) (adjustedAngleY + ((j >> 2) - 1) * 0.8F + 0.65F) - 64;
                            int offsetZ = (int) (adjustedAngleZ + (j >> 1 & 1) * 0.6F - 0.3F) - 64;
                            if (offsetX < 0 || offsetY < 0 || offsetZ < 0 || offsetX >= 64 || offsetY >= 64 || offsetZ >= 64 || blockData[offsetX + offsetY * 64 + offsetZ * 4096] > 0) {
                                if (i == 1) {
                                    if (M[32] > 0 && rotationY > 0.0F) {
                                        M[32] = 0;
                                        rotationY = -0.1F;
                                    } else {
                                        rotationY = 0.0F;
                                    }
                                }
                                continue label266;
                            }
                        }

                        cameraAngleX = adjustedAngleX;
                        cameraAngleY = adjustedAngleY;
                        cameraAngleZ = adjustedAngleZ;
                    }
                }

                int breakingStageX = 0;
                int breakingStageY = 0;
                // Break block if left mouse button is pressed and there's a selected block
                if (M[1] > 0 && selectedBlockIndex > 0) {
                    blockData[selectedBlockIndex] = 0;
                    M[1] = 0;
                }

                // Place block if right mouse button is pressed and there's a selected block
                if (M[0] > 0 && selectedBlockIndex > 0) {
                    blockData[selectedBlockIndex + selectedBlockType] = 1;
                    M[0] = 0;
                }

                // Clear neighboring blocks for smooth rendering
                for (int i = 0; i < 12; ++i) {
                    int offsetX = (int) (cameraAngleX + (i >> 0 & 1) * 0.6F - 0.3F) - 64;
                    int offsetY = (int) (cameraAngleY + ((i >> 2) - 1) * 0.8F + 0.65F) - 64;
                    int offsetZ = (int) (cameraAngleZ + (i >> 1 & 1) * 0.6F - 0.3F) - 64;
                    if (offsetX >= 0 && offsetY >= 0 && offsetZ >= 0 && offsetX < 64 && offsetY < 64 && offsetZ < 64) {
                        blockData[offsetX + offsetY * 64 + offsetZ * 4096] = 0;
                    }
                }

                float closestBlockDistance = -1.0F;

                for (int viewX = 0; viewX < 214; ++viewX) {
                    float horizontalAngle = (viewX - 107) / 90.0F;

                    for (int viewY = 0; viewY < 120; ++viewY) {
                        float verticalAngle = (viewY - 60) / 90.0F;
                        float f21 = 1.0F;
                        float f22 = f21 * cosRotationY + verticalAngle * sinRotationY;
                        float sinVertical = verticalAngle * cosRotationY - f21 * sinRotationY;
                        float cosHorizontal = horizontalAngle * cosRotationX + f22 * sinRotationX;
                        float sinHorizontal = f22 * cosRotationX - horizontalAngle * sinRotationX;
                        int finalBlockColor = 0;
                        int finalVisibilityFactor = 255;
                        double visibilityThreshold = 20.0;
                        float f26 = 5.0F;

                        for (int axis = 0; axis < 3; ++axis) {
                            float currentAxisValue = cosHorizontal;
                            if (axis == 1) {
                                currentAxisValue = sinVertical;
                            }

                            if (axis == 2) {
                                currentAxisValue = sinHorizontal;
                            }

                            float axisInverseAbs = 1.0F / (currentAxisValue < 0.0F ? -currentAxisValue : currentAxisValue);
                            float rotatedX = cosHorizontal * axisInverseAbs;
                            float rotatedY = sinVertical * axisInverseAbs;
                            float rotatedZ = sinHorizontal * axisInverseAbs;

                            float blockOffset = cameraAngleX - ((int) cameraAngleX);
                            if (axis == 1) {
                                blockOffset = cameraAngleY - ((int) cameraAngleY);
                            }

                            if (axis == 2) {
                                blockOffset = cameraAngleZ - ((int) cameraAngleZ);
                            }

                            if (currentAxisValue > 0.0F) {
                                blockOffset = 1.0F - blockOffset;
                            }

                            float visibilityFactor = axisInverseAbs * blockOffset;
                            float blockX = cameraAngleX + rotatedX * blockOffset;
                            float blockY = cameraAngleY + rotatedY * blockOffset;
                            float blockZ = cameraAngleZ + rotatedZ * blockOffset;
                            if (currentAxisValue < 0.0F) {
                                if (axis == 0) {
                                    --blockX;
                                }

                                if (axis == 1) {
                                    --blockY;
                                }

                                if (axis == 2) {
                                    --blockZ;
                                }
                            }

                            while (visibilityFactor < visibilityThreshold) {
                                int blockPosX = (int) blockX - 64;
                                int blockPosY = (int) blockY - 64;
                                int blockPosZ = (int) blockZ - 64;
                                if (blockPosX < 0 || blockPosY < 0 || blockPosZ < 0 || blockPosX >= 64 || blockPosY >= 64 || blockPosZ >= 64) {
                                    break;
                                }

                                int blockIndex = blockPosX + blockPosY * 64 + blockPosZ * 4096;
                                int blockType = blockData[blockIndex];
                                if (blockType > 0) {
                                    breakingStageX = (int) ((blockX + blockZ) * 16.0F) & 15;
                                    breakingStageY = ((int) (blockY * 16.0F) & 15) + 16;
                                    if (axis == 1) {
                                        breakingStageX = (int) (blockX * 16.0F) & 15;
                                        breakingStageY = (int) (blockZ * 16.0F) & 15;
                                        if (rotatedY < 0.0F) {
                                            breakingStageY += 32;
                                        }
                                    }

                                    int blockColor = 0xffffff;
                                    if (blockIndex != selectedBlockIndex || breakingStageX > 0 && breakingStageY % 16 > 0 && breakingStageX < 15 && breakingStageY % 16 < 15) {
                                        blockColor = colorArray[breakingStageX + breakingStageY * 16 + blockType * 256 * 3];
                                    }

                                    if (visibilityFactor < f26 && viewX == M[2] / 4 && viewY == M[3] / 4) {
                                        closestBlockDistance = blockIndex;
                                        selectedBlockType = 1;
                                        if (currentAxisValue > 0.0F) {
                                            selectedBlockType = -1;
                                        }

                                        selectedBlockType <<= 6 * axis;
                                        f26 = visibilityFactor;
                                    }

                                    if (blockColor > 0) {
                                        finalBlockColor = blockColor;
                                        finalVisibilityFactor = 255 - (int) (visibilityFactor / 20.0F * 255.0F);
                                        finalVisibilityFactor = finalVisibilityFactor * (255 - (axis + 2) % 3 * 50) / 255;
                                        visibilityThreshold = visibilityFactor;
                                    }
                                }

                                blockX += rotatedX;
                                blockY += rotatedY;
                                blockZ += rotatedZ;
                                visibilityFactor += axisInverseAbs;
                            }
                        }

                        int red = (finalBlockColor >> 16 & 0xFF) * finalVisibilityFactor / 255;
                        int green = (finalBlockColor >> 8 & 0xFF) * finalVisibilityFactor / 255;
                        int blue = (finalBlockColor & 0xFF) * finalVisibilityFactor / 255;
                        pixelData[viewX + viewY * 214] = red << 16 | green << 8 | blue;
                    }
                }

                selectedBlockIndex = (int) closestBlockDistance;
                final BufferStrategy bufferStrategy = frame.getBufferStrategy();
                Graphics g = bufferStrategy.getDrawGraphics();
                g.drawImage(localBufferedImage, 0, 0, frame.getWidth(), frame.getHeight(), null);
                g.dispose();
                bufferStrategy.show();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean handleEvent(Event event) {
        int i = 0;
        switch (event.id) {
            case Event.KEY_PRESS:
                i = 1;
            case Event.KEY_RELEASE:
                M[event.key] = i;
                break;
            case Event.MOUSE_DOWN:
                i = 1;
                M[2] = event.x;
                M[3] = event.y;
            case Event.MOUSE_UP:
                if ((event.modifiers & Event.META_MASK) > 0) {
                    M[1] = i;
                } else {
                    M[0] = i;
                }
                break;
            case Event.MOUSE_MOVE:
            case Event.MOUSE_DRAG:
                M[2] = event.x;
                M[3] = event.y;
                break;
            case Event.MOUSE_EXIT:
                M[2] = 0;
        }

        return true;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            M[0] = 1;
        } else {
            M[1] = 1;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        M[2] = e.getX();
        M[3] = e.getY();
    }



    @Override
    public void mouseMoved(MouseEvent e) {
        M[2] = e.getX();
        M[3] = e.getY();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        M[2] = 0;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }



    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        M[e.getKeyCode()] = 0;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        M[e.getKeyCode()] = 1;
    }

    public static void main(String[] args) {
        new Minecraft4K();
    }
}
