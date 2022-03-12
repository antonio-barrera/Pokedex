/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Ventanas;


import Modelo.AccesoDatosJDBC;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Hashtable;
import java.util.Objects;
import javax.imageio.ImageIO;

/**
 *
 * @author linda
 */
public class VentanaPokedex extends javax.swing.JFrame {
    private final String relleno = "- - - - - -";
    private final BufferedImage buffer1;
    private int limit;
    private int contador = -1;
    private String USER = "";
    private Image imagen1;
    private ResultSet resultadoConsulta;
    private Hashtable hash;
    private String banderaFiltro = "";
    
    public void setUSER(String USER) {
        this.USER = USER;
    }
    
    private void filtrarResultados(int columna, String tabla, String consulta) {
        hash.clear();
        int cont = 0;
        try {
            resultadoConsulta = AccesoDatosJDBC.ejecutarConsulta(tabla, consulta);
            while(resultadoConsulta.next()) {
                if (cont+1 != resultadoConsulta.getInt(columna) && "TODOS".equals(banderaFiltro)) {
                    hash.put(cont, "|" + (cont+1));
                } else {
                    hash.put(cont, resultadoConsulta.getInt(columna));
                }
                cont++;
            }
            limit = cont - 1;
            contador = -1;
        } catch (SQLException ex) {
            ex.printStackTrace(System.out);
        }
    }
    
    private void filtrarTodos() {
        banderaFiltro = "TODOS";
        int columna = 1;
        String tabla = "pokemon ";
        String consulta = "";
        filtrarResultados(columna, tabla, consulta);
        btnAgregarFavoritos.setEnabled(false);
        btnEliminarFavoritos.setEnabled(false);
        btnBuscarNombre.setEnabled(true);
        txtBuscarNombre.setEnabled(true);
        resultadoVacio();
    }
    
    private void filtrarFavoritos() {
        banderaFiltro = "FAVORITOS";
        int columna = 3;
        String tabla = "favoritos ";
        String consulta = "WHERE usuario = '" + USER + "' order by pokemon_id";
        filtrarResultados(columna, tabla, consulta);
        btnAgregarFavoritos.setEnabled(false);
        btnEliminarFavoritos.setEnabled(true);
        btnBuscarNombre.setEnabled(false);
        txtBuscarNombre.setEnabled(false);
        resultadoVacio();
    }
    
    private void filtrarNoFavoritos() {
        banderaFiltro = "NOFAVORITOS";
        String excluidos = "";
        String tablaFavoritos = "favoritos ";
        String tablaPokemon = "pokemon ";
        String consultaExcluidos = "WHERE usuario = '" + USER + "' order by pokemon_id";
        
        filtrarResultados(3, tablaFavoritos, consultaExcluidos);
        if (hash.size() > 0) {
            excluidos = "WHERE id != ";
            excluidos += hash.get(0);
            for (int i = 1; i <= limit; i++) {
                excluidos += " AND id != " + hash.get(i);
            }
        } 
        filtrarResultados(1, tablaPokemon, excluidos);
        btnAgregarFavoritos.setEnabled(true);
        btnEliminarFavoritos.setEnabled(false);
        btnBuscarNombre.setEnabled(false);
        txtBuscarNombre.setEnabled(false);
        resultadoVacio();
    }
    
    private void resultadoEncontrado(ResultSet rs) {
        try {
            lblIdReal.setText(rs.getString(1));
            nombrePokemon.setText(rs.getString(2));
            lblGeneracion.setText(rs.getString(5));
            lblAltura.setText(rs.getString(10));
            lblPeso.setText(rs.getString(11));
            lblEspecie.setText(rs.getString(12));
            lblColor.setText(rs.getString(13));
            lblHabitat.setText(rs.getString(15));
            lblCaptura.setText(rs.getString(17));
            lblExperiencia.setText(rs.getString(18));
            lblFelicidad.setText(rs.getString(19));
        } catch (SQLException ex) {
            ex.printStackTrace(System.out);
        }
    }
    
    private void resultadoNoEncontrado(String relleno) {
        lblIdReal.setText(relleno);
        nombrePokemon.setText(relleno);
        lblGeneracion.setText(relleno);
        lblAltura.setText(relleno);
        lblPeso.setText(relleno);
        lblEspecie.setText(relleno);
        lblColor.setText(relleno);
        lblHabitat.setText(relleno);
        lblCaptura.setText(relleno);
        lblExperiencia.setText(relleno);
        lblFelicidad.setText(relleno);
        dibujaElPokemonQueEstaEnLaPosicion(-1);
    }
    
    private void llenarLabeles(ResultSet rs) {
        try {
            if(rs.next()) {
                resultadoEncontrado(rs);
            } else {
                resultadoNoEncontrado(relleno);
            }
        } catch (SQLException ex) {
            ex.printStackTrace(System.out);
        }   
    }
    
    private void resultadoVacio() {
        dibujaElPokemonQueEstaEnLaPosicion(-1);
        resultadoNoEncontrado(relleno);
    }
    
    private void ejecutarPagineo(int contador) {
        String pokemon = String.valueOf(hash.get(contador));
        pokemon = pokemon.replace("|", "");
        String tabla = "pokemon ";
        String consulta = "WHERE id = " + pokemon;
        
        if (!Objects.equals(pokemon, "null")) {
            resultadoConsulta = AccesoDatosJDBC.ejecutarConsulta(tabla, consulta);
            llenarLabeles(resultadoConsulta);
            dibujaElPokemonQueEstaEnLaPosicion(Integer.parseInt(pokemon)-1);
        }
    }
    
    public void dibujaElPokemonQueEstaEnLaPosicion(int posicion){
        int fila = posicion / 31;
        int columna = posicion % 31;
        Graphics2D g2 = (Graphics2D) buffer1.getGraphics();
        g2.setColor(Color.black);
        g2.fillRect(0, 0, //pinta el fondo del jpanel negro
                imagenPokemon.getWidth(),
                imagenPokemon.getHeight()); 
                g2.drawImage(imagen1,
                0,  //posicion X inicial dentro del jpanel 
                0,  // posicion Y inicial dentro del jpanel
                imagenPokemon.getWidth(), //ancho del jpanel
                imagenPokemon.getHeight(), //alto del jpanel
                columna*96, //posicion inicial X dentro de la imagen de todos los pokemon
                fila*96, //posicion inicial Y dentro de la imagen de todos los pokemon
                columna*96 + 96, //posicion final X
                fila*96 + 96, //posicion final Y
                null  //si no lo pones no va
                );
        repaint();
    }
    
    @Override
    public void paint(Graphics g){
        super.paintComponents(g);
        Graphics2D  g2 = (Graphics2D) imagenPokemon.getGraphics();
        g2.drawImage(buffer1,0,0,imagenPokemon.getWidth(), imagenPokemon.getHeight(),null);
    }
    
    /**
     * Creates new form VentanaPokedex
     */
    public VentanaPokedex() {
        this.hash = new Hashtable();
        initComponents();
        try {
            //imagen1 = ImageIO.read(getClass().getResource("/imagenes/black-white.png"));*/
            imagen1 = ImageIO.read(new File("C:\\tmp2\\datos\\imagenes\\black-white.png"));
        } catch (IOException ex) {
            ex.printStackTrace(System.out);
        }
        buffer1 = (BufferedImage) imagenPokemon.createImage(imagenPokemon.getWidth(), imagenPokemon.getHeight());
        Graphics2D g2 = buffer1.createGraphics();
        filtrarTodos(); 
    }
     
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton3 = new javax.swing.JButton();
        buttonGroup1 = new javax.swing.ButtonGroup();
        imagenPokemon = new javax.swing.JPanel();
        der = new javax.swing.JButton();
        izq = new javax.swing.JButton();
        nombrePokemon = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        txtBuscarNombre = new javax.swing.JTextField();
        btnBuscarNombre = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        rbtnFavoritos = new javax.swing.JRadioButton();
        rbtnNoFavoritos = new javax.swing.JRadioButton();
        rbtnTodos = new javax.swing.JRadioButton();
        btnAgregarFavoritos = new javax.swing.JButton();
        btnEliminarFavoritos = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        lblId = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        lblAltura = new javax.swing.JLabel();
        lblPeso = new javax.swing.JLabel();
        lblEspecie = new javax.swing.JLabel();
        lblColor = new javax.swing.JLabel();
        lblHabitat = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        lblGeneracion = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        lblCaptura = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        lblFelicidad = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        lblExperiencia = new javax.swing.JLabel();
        btnLogout = new javax.swing.JButton();
        btnFiltros = new javax.swing.JButton();
        lblIdReal = new javax.swing.JLabel();

        jButton3.setText("jButton3");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 0, 0));

        imagenPokemon.setBorder(new javax.swing.border.MatteBorder(null));

        javax.swing.GroupLayout imagenPokemonLayout = new javax.swing.GroupLayout(imagenPokemon);
        imagenPokemon.setLayout(imagenPokemonLayout);
        imagenPokemonLayout.setHorizontalGroup(
            imagenPokemonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 200, Short.MAX_VALUE)
        );
        imagenPokemonLayout.setVerticalGroup(
            imagenPokemonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        der.setText("Derecha ==>");
        der.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                derActionPerformed(evt);
            }
        });

        izq.setText("<== Izquierda");
        izq.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                izqActionPerformed(evt);
            }
        });

        nombrePokemon.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        nombrePokemon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        nombrePokemon.setText("- - - - - -");
        nombrePokemon.setToolTipText("");

        jLabel1.setText("Nombre:");

        btnBuscarNombre.setText("Buscar");
        btnBuscarNombre.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarNombreActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Rockwell", 1, 36)); // NOI18N
        jLabel2.setText("Pokedex");

        buttonGroup1.add(rbtnFavoritos);
        rbtnFavoritos.setText("Favoritos");
        rbtnFavoritos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbtnFavoritosActionPerformed(evt);
            }
        });

        buttonGroup1.add(rbtnNoFavoritos);
        rbtnNoFavoritos.setText("No Favoritos");
        rbtnNoFavoritos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbtnNoFavoritosActionPerformed(evt);
            }
        });

        buttonGroup1.add(rbtnTodos);
        rbtnTodos.setSelected(true);
        rbtnTodos.setText("Todos");
        rbtnTodos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbtnTodosActionPerformed(evt);
            }
        });

        btnAgregarFavoritos.setText("Agregar Favorito");
        btnAgregarFavoritos.setToolTipText("");
        btnAgregarFavoritos.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        btnAgregarFavoritos.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnAgregarFavoritos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAgregarFavoritosActionPerformed(evt);
            }
        });

        btnEliminarFavoritos.setText("Eliminar Favorito");
        btnEliminarFavoritos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarFavoritosActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jLabel3.setText("Id:");

        jLabel4.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jLabel4.setText("Altura:");

        jLabel5.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jLabel5.setText("Peso:");

        jLabel6.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jLabel6.setText("Especie:");

        jLabel7.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jLabel7.setText("Color:");

        jLabel8.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jLabel8.setText("Habitat:");

        lblAltura.setText("- - - - - -");

        lblPeso.setText("- - - - - -");

        lblEspecie.setText("- - - - - -");

        lblColor.setText("- - - - - -");

        lblHabitat.setText("- - - - - -");

        jLabel9.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jLabel9.setText("Generación:");

        lblGeneracion.setText("- - - - - -");

        jLabel10.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jLabel10.setText("Captura:");

        lblCaptura.setText("- - - - - -");

        jLabel11.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jLabel11.setText("Experiencia:");

        lblFelicidad.setText("- - - - - -");

        jLabel12.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jLabel12.setText("Felicidad:");

        lblExperiencia.setText("- - - - - -");

        btnLogout.setText("Cerrar Sesión");
        btnLogout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogoutActionPerformed(evt);
            }
        });

        btnFiltros.setText("Filtros");
        btnFiltros.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFiltrosActionPerformed(evt);
            }
        });

        lblIdReal.setText("- - - - - -");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(rbtnFavoritos)
                        .addComponent(rbtnNoFavoritos)
                        .addComponent(rbtnTodos)
                        .addComponent(izq)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblColor, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                            .addComponent(lblEspecie, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblPeso, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                            .addComponent(lblAltura, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                            .addComponent(lblIdReal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lblId, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(lblHabitat, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                                    .addComponent(lblGeneracion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(lblCaptura, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(lblExperiencia, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(lblFelicidad, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(der)
                        .addGap(19, 19, 19))))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(btnLogout)
                        .addGap(73, 73, 73)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(138, 138, 138)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(nombrePokemon, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(btnFiltros)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(imagenPokemon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(btnEliminarFavoritos)
                                        .addComponent(btnAgregarFavoritos, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)))))))
                .addContainerGap(23, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(txtBuscarNombre, javax.swing.GroupLayout.PREFERRED_SIZE, 264, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnBuscarNombre)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnLogout)
                    .addComponent(btnFiltros))
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtBuscarNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(btnBuscarNombre))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(imagenPokemon, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nombrePokemon, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(50, 50, 50)
                                .addComponent(btnAgregarFavoritos)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnEliminarFavoritos))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(33, 33, 33)
                                .addComponent(rbtnFavoritos)
                                .addGap(18, 18, 18)
                                .addComponent(rbtnNoFavoritos)
                                .addGap(13, 13, 13)
                                .addComponent(rbtnTodos)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 42, Short.MAX_VALUE)))
                .addComponent(lblId)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8)
                            .addComponent(lblHabitat)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblIdReal))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel9)
                            .addComponent(lblGeneracion)
                            .addComponent(jLabel4)
                            .addComponent(lblAltura))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel10)
                            .addComponent(lblCaptura)
                            .addComponent(lblPeso))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel11)
                            .addComponent(lblExperiencia)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(lblEspecie))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(lblFelicidad)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblColor))
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(izq)
                    .addComponent(der))
                .addGap(25, 25, 25))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
   
    private void izqActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_izqActionPerformed
        contador--;
        if(contador < 0){
            contador = 0;
        }
        ejecutarPagineo(contador);
    }//GEN-LAST:event_izqActionPerformed

    private void derActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_derActionPerformed
        contador++;
        if (contador > limit){
            contador = limit;
        }
        ejecutarPagineo(contador); 
    }//GEN-LAST:event_derActionPerformed

    private void btnAgregarFavoritosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarFavoritosActionPerformed
        boolean prueba = !(lblIdReal.getText().equals(relleno));
        if(prueba) {
            AccesoDatosJDBC.ejecutarInsertFavoritos(USER, Integer.parseInt(lblIdReal.getText()));
            filtrarNoFavoritos();
            resultadoVacio();
        }
    }//GEN-LAST:event_btnAgregarFavoritosActionPerformed

    private void btnEliminarFavoritosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarFavoritosActionPerformed
        boolean prueba = !(lblIdReal.getText().equals(relleno));
        if(prueba) {
            AccesoDatosJDBC.ejecutarDeleteFavoritos(Integer.parseInt(lblIdReal.getText()));
            filtrarFavoritos();
            resultadoVacio();
        }
    }//GEN-LAST:event_btnEliminarFavoritosActionPerformed

    private void btnBuscarNombreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarNombreActionPerformed
        try {
            String nombre = txtBuscarNombre.getText().substring(0, 1).toUpperCase() + txtBuscarNombre.getText().substring(1).toLowerCase();
            resultadoConsulta = AccesoDatosJDBC.ejecutarConsulta("pokemon ", "WHERE name = '" + nombre + "'");
            llenarLabeles(resultadoConsulta);
            if(!Objects.equals(lblIdReal.getText(), relleno)) {
                contador = Integer.valueOf(resultadoConsulta.getString(1)) - 1;
                dibujaElPokemonQueEstaEnLaPosicion(contador);
            }
        } catch (SQLException ex) {
            ex.printStackTrace(System.out);
        }
    }//GEN-LAST:event_btnBuscarNombreActionPerformed
       
    private void rbtnFavoritosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbtnFavoritosActionPerformed
        filtrarFavoritos();
    }//GEN-LAST:event_rbtnFavoritosActionPerformed

    private void rbtnNoFavoritosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbtnNoFavoritosActionPerformed
        filtrarNoFavoritos();
    }//GEN-LAST:event_rbtnNoFavoritosActionPerformed
    
    private void rbtnTodosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbtnTodosActionPerformed
        filtrarTodos();
    }//GEN-LAST:event_rbtnTodosActionPerformed

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
        Login ventanaLogin = new Login();
        ventanaLogin.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnLogoutActionPerformed

    private void btnFiltrosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFiltrosActionPerformed
        Filtros ventanaFiltros = new Filtros();
        ventanaFiltros.setUSER(USER);
        ventanaFiltros.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnFiltrosActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAgregarFavoritos;
    private javax.swing.JButton btnBuscarNombre;
    private javax.swing.JButton btnEliminarFavoritos;
    private javax.swing.JButton btnFiltros;
    private javax.swing.JButton btnLogout;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton der;
    private javax.swing.JPanel imagenPokemon;
    private javax.swing.JButton izq;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel lblAltura;
    private javax.swing.JLabel lblCaptura;
    private javax.swing.JLabel lblColor;
    private javax.swing.JLabel lblEspecie;
    private javax.swing.JLabel lblExperiencia;
    private javax.swing.JLabel lblFelicidad;
    private javax.swing.JLabel lblGeneracion;
    private javax.swing.JLabel lblHabitat;
    private javax.swing.JLabel lblId;
    private javax.swing.JLabel lblIdReal;
    private javax.swing.JLabel lblPeso;
    private javax.swing.JLabel nombrePokemon;
    private javax.swing.JRadioButton rbtnFavoritos;
    private javax.swing.JRadioButton rbtnNoFavoritos;
    private javax.swing.JRadioButton rbtnTodos;
    private javax.swing.JTextField txtBuscarNombre;
    // End of variables declaration//GEN-END:variables
}