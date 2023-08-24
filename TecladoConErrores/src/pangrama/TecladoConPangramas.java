package pangrama;




import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.io.*;
import java.util.ArrayList;


public class TecladoConPangramas extends JFrame {
    private JTextArea textoEsperado;
    private JTextArea textoTipeado;
    private ArrayList<String> pangramas;
    private int posicionPangrama = 0;
    private String pangramaActual;
    private int caracteresCorrectos = 0;
    private int caracteresIncorrectos = 0;
    private int caracteresTotales = 0;
    private boolean procesamientoActivo = false;
    private boolean shiftPresionado = false;
    private String textoUsuario = "";
    private String mensajeAnterior = null; 
    private int aciertosFraseActual = 0; 
    private int erroresFraseActual = 0; 
    private int aciertosAcumulados = 0; 
    private int erroresAcumulados = 0; 
    private boolean primeraVez = true; 
    private long inicio = 0; 

    public TecladoConPangramas() {
        setTitle("Teclado con Pangramas y Monitoreo de Precisión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 500);
        setLayout(new BorderLayout());

        cargarPangramasDesdeArchivo(); 

        textoEsperado = new JTextArea();
        textoEsperado.setEditable(false);
        actualizarPangrama();
        add(textoEsperado, BorderLayout.NORTH);

        textoTipeado = new JTextArea();
        textoTipeado.setLineWrap(true);
        textoTipeado.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textoTipeado);
        add(scrollPane, BorderLayout.CENTER);

        JPanel tecladoVirtual = new JPanel(new GridLayout(4, 10));
        add(tecladoVirtual, BorderLayout.SOUTH);

        String[] teclas = {
            "1", "2", "3", "4", "5", "6", "7", "8", "9", "0",
            "q", "w", "e", "r", "t", "y", "u", "i", "o", "p",
            "a", "s", "d", "f", "g", "h", "j", "k", "l", "ENTER",
            "SHIFT", "z", "x", "c", "v", "b", "n", "m", "BORRAR",
            "ESPACIO"
        };

        for (String tecla : teclas) {
            JButton boton = new JButton(tecla);
            boton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!procesamientoActivo) {
                        if (tecla.equals("ENTER")) {
                            procesarTextoTipeado();
                        } else if (tecla.equals("BORRAR")) {
                            borrarUltimoCaracter();
                        } else if (tecla.equals("SHIFT")) {
                            shiftPresionado = !shiftPresionado;
                            actualizarTeclado(tecladoVirtual);
                        } else if (tecla.equals("ESPACIO")) {
                            textoTipeado.append(" ");
                        } else {
                            char caracter = tecla.charAt(0);
                            if (Character.isLetter(caracter)) {
                                if (shiftPresionado) {
                                    caracter = Character.toUpperCase(caracter);
                                } else {
                                    caracter = Character.toLowerCase(caracter);
                                }
                            }
                            textoTipeado.append(String.valueOf(caracter));
                        }
                    }
                }
            });
            tecladoVirtual.add(boton);
        }

        setFocusable(true);
        requestFocus();

        textoTipeado.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                actualizarPrecision();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                actualizarPrecision();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                actualizarPrecision();
            }
        });
    }

    private void cargarPangramasDesdeArchivo() {
        pangramas = new ArrayList<>();

        try {
            FileReader fileReader = new FileReader("pangramas.txt");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String linea;

            while ((linea = bufferedReader.readLine()) != null) {
                pangramas.add(linea);
            }

            bufferedReader.close();
        } catch (IOException e) {
            System.err.println("Error al cargar los pangramas desde el archivo.");
            e.printStackTrace();
        }
    }

    private void actualizarTeclado(JPanel tecladoVirtual) {
        Component[] componentes = tecladoVirtual.getComponents();
        for (Component componente : componentes) {
            if (componente instanceof JButton) {
                JButton boton = (JButton) componente;
                String etiqueta = boton.getText();
                if (etiqueta.length() == 1) {  
                    char caracter = etiqueta.charAt(0);
                    if (Character.isLetter(caracter)) {
                        if (shiftPresionado) {
                            caracter = Character.toUpperCase(caracter);
                        } else {
                            caracter = Character.toLowerCase(caracter);
                        }
                        boton.setText(String.valueOf(caracter));
                    }
                }
            }
        }
    }

    private void actualizarPangrama() {
        if (posicionPangrama < pangramas.size()) {
            pangramaActual = pangramas.get(posicionPangrama);
            textoEsperado.setText(pangramaActual);
        } else {
            pangramaActual = null;
            textoEsperado.setText("¡Completado!");
        }
    }

    private void procesarTextoTipeado() {
        if (procesamientoActivo) {
            return;
        }

        procesamientoActivo = true;

        String tipeado = textoTipeado.getText();
        if (pangramaActual == null) {
            procesamientoActivo = false;
            return;
        }

        caracteresTotales += pangramaActual.length();

        for (int i = 0; i < pangramaActual.length(); i++) {
            if (i < tipeado.length()) {
                char caracterTipeado = tipeado.charAt(i);
                char caracterEsperado = pangramaActual.charAt(i);

                if (caracterTipeado == caracterEsperado) {
                    if (caracterTipeado != ' ') { 
                        caracteresCorrectos++;
                        aciertosFraseActual++; 
                    }
                } else {
                    caracteresIncorrectos++;
                    erroresFraseActual++; 
                }
            } else {
                caracteresIncorrectos++;
                erroresFraseActual++; 
            }
        }

        mensajeAnterior = pangramaActual; 
        textoUsuario = tipeado;

        if (primeraVez) {
            aciertosAcumulados = aciertosFraseActual;
            erroresAcumulados = erroresFraseActual;
            
            primeraVez = false;
        } else {
            aciertosAcumulados += aciertosFraseActual;
            erroresAcumulados += erroresFraseActual;
        }

        posicionPangrama++;

        if (posicionPangrama < pangramas.size()) {
            pangramaActual = pangramas.get(posicionPangrama);
            textoEsperado.setText(pangramaActual);
        } else {
            pangramaActual = null;
            textoEsperado.setText("¡Completado!");
        }

        textoTipeado.setText("");
        mostrarResultados();
        aciertosFraseActual = 0; 
        erroresFraseActual = 0; 
        procesamientoActivo = false;
    }

    private void borrarUltimoCaracter() {
        String textoTipeadoActual = textoTipeado.getText();
        if (!textoTipeadoActual.isEmpty()) {
            textoTipeado.setText(textoTipeadoActual.substring(0, textoTipeadoActual.length() - 1));
        }
    }

    private void actualizarPrecision() {
        double precision = 0;
        if (caracteresTotales > 0) {
            precision = ((double) caracteresCorrectos / caracteresTotales) * 100;
        }

        System.out.println("Aciertos: " + caracteresCorrectos);
        System.out.println("Errores: " + caracteresIncorrectos);
        System.out.println("Precisión del usuario: " + precision + "%");
    }

    private void mostrarResultados() {
        if (mensajeAnterior == null) {
            mensajeAnterior = "No hay mensaje anterior.";
        }

        double tiempo = System.currentTimeMillis() - inicio;
        double velocidad = ((caracteresCorrectos + caracteresIncorrectos) / tiempo) * 60000;
        double precisionUsuario = ((double) caracteresCorrectos / caracteresTotales) * 100;

        JFrame ventanaResultados = new JFrame("Resultados");
        ventanaResultados.setSize(400, 300);
        ventanaResultados.setLayout(new BorderLayout());

        JTextArea resultadosTexto = new JTextArea();
        resultadosTexto.setEditable(false);

        StringBuilder mensajeResultados = new StringBuilder();
        mensajeResultados.append("Mensaje Anterior:\n");
        mensajeResultados.append(mensajeAnterior);
        mensajeResultados.append("\n\nTexto Tipeado por el Usuario:\n");
        mensajeResultados.append(textoUsuario);
        mensajeResultados.append("\n\nAciertos de la Frase Actual: ");
        mensajeResultados.append(aciertosFraseActual);
        mensajeResultados.append("\nErrores de la Frase Actual: ");
        mensajeResultados.append(erroresFraseActual);
        mensajeResultados.append("\n\nAciertos Acumulados: ");
        mensajeResultados.append(aciertosAcumulados);
        mensajeResultados.append("\nErrores Acumulados: ");
        mensajeResultados.append(erroresAcumulados);
        mensajeResultados.append("\n\nVelocidad de Escritura: ");
        mensajeResultados.append(velocidad);
        mensajeResultados.append(" caracteres por minuto");
        mensajeResultados.append("\nPrecisión del Usuario: ");
        mensajeResultados.append(precisionUsuario);
        mensajeResultados.append("%");

        resultadosTexto.setText(mensajeResultados.toString());

        JScrollPane scrollPane = new JScrollPane(resultadosTexto);
        ventanaResultados.add(scrollPane, BorderLayout.CENTER);

        ventanaResultados.setVisible(true);
    }

    public void iniciar() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                TecladoConPangramas teclado = new TecladoConPangramas();
                teclado.setVisible(true);
            }
        });
    }
}
