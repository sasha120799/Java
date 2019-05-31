import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class UI extends JFrame{
    private JPanel catalogPanel= new JPanel();
    private JList filesList = new JList();
    private JScrollPane filesScroll = new JScrollPane(filesList);
    private JPanel buttonsPanel = new JPanel();
    private JButton addButton = new JButton("Создать папку");
    private JButton backButton = new JButton("Назад");
    private JButton delButton = new JButton("Удалить");
    private JButton renameButton = new JButton("Переименновать");
    private JButton addFileButton = new JButton("Создать файл");
    private JButton reFreash = new JButton("Обновить");
    private ArrayList <String> dirsCache = new ArrayList();
    private File buffer = null;
    private String bufferSelect ;

    public UI(){
        super("Проводник");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        catalogPanel.setLayout(new BorderLayout(5,5));
        catalogPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        buttonsPanel.setLayout(new GridLayout(2,3,5,5));
        JDialog createNewDirDialog = new JDialog(UI.this, "Создание папки", true);
        JPanel createNewDirPanel = new JPanel();
        createNewDirDialog.add(createNewDirPanel);
        JDialog createNewFileDialog = new JDialog(UI.this, "Создание файла", true);
        JPanel createNewFilePanel = new JPanel();
        createNewFileDialog.add(createNewFilePanel);
        File disc[] = File.listRoots();
        filesScroll.setPreferredSize(new Dimension(400,500));
        filesList.setListData(disc);
        filesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        filesList.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount()==2){
                    DefaultListModel model = new DefaultListModel();
                    String selectedObject = filesList.getSelectedValue().toString();
                    String fullPath = toFullPath(dirsCache);
                    File selectedFile;
                    if (dirsCache.size()>1){
                        selectedFile = new File(fullPath,selectedObject);
                    } else {
                        selectedFile = new File(fullPath + selectedObject);
                    }

                    if(selectedFile.isDirectory()){
                        String[] rootStr = selectedFile.list();
                        for (String str : rootStr){
                            File checkObject = new File (selectedFile.getPath(), str);
                            if (!checkObject.isHidden()){
                                if (checkObject.isDirectory()){
                                    model.addElement(str);
                                }else{
                                    model.addElement("файл - "+str+ " его размер - "+ checkObject.length());
                                }
                            }
                        }

                        dirsCache.add(selectedObject);
                        filesList.setModel(model);
                    } else{
                        selectedFile = new File(fullPath,toNameFile(selectedObject));
                        try {
                            Desktop.getDesktop().open(selectedFile);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }


                }

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
        filesList.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode()==KeyEvent.VK_C&&e.isControlDown()){
                    String selectedObject = filesList.getSelectedValue().toString();
                    String fullPath = toFullPath(dirsCache);
                    if (dirsCache.size()>1){
                        buffer = new File(fullPath,toNameFile(selectedObject));
                        bufferSelect = toNameFile(selectedObject);
                    }
                }
                if (e.getKeyCode()==KeyEvent.VK_V&&e.isControlDown()){
                    String fullPath = toFullPath(dirsCache);
                    InputStream is = null;
                    OutputStream os = null;
                    if (dirsCache.size()>=1){
                        File copyFile = new File(fullPath,bufferSelect);
                        if (!buffer.toPath().toString().equals(copyFile.toPath().toString())) {
                            try {
                                copyFile.createNewFile();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                            try {

                                is = new FileInputStream(buffer.toPath().toString());
                                os = new FileOutputStream(copyFile.toPath().toString());
                                byte[] buffer = new byte[1024];
                                int length;
                                while ((length = is.read(buffer)) > 0) {
                                    os.write(buffer, 0, length);
                                }
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            } finally {
                                try {
                                    is.close();
                                    os.close();
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }

                            }
                        }
                    }
                    File updateDir = new File (fullPath);
                    String updateMas[] = updateDir.list();
                    DefaultListModel updateModel = new DefaultListModel();
                    for (String str : updateMas){
                        File check = new File (updateDir.getPath(), str);
                        if(!check.isHidden()){
                            if(check.isDirectory()){
                                updateModel.addElement(str);
                            }else{
                                updateModel.addElement("файл - "+str+ " его размер - "+ check.length());
                            }
                        }
                    }
                    filesList.setModel(updateModel);

                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (dirsCache.size()>1){
                    dirsCache.remove(dirsCache.size()-1);
                    String backDir = toFullPath(dirsCache);
                    String[] objects = new File(backDir).list();
                    DefaultListModel backRootModel = new DefaultListModel();

                    for (String str : objects){
                        File chechFile = new File(backDir, str);
                        if(!chechFile.isHidden()){
                            if(chechFile.isDirectory()){
                                backRootModel.addElement(str);
                            }else{
                                backRootModel.addElement("файл - "+str+ " его размер - "+ chechFile.length());
                            }
                        }
                    }
                    filesList.setModel(backRootModel);
                }else{
                    dirsCache.removeAll(dirsCache);
                    filesList.setListData(disc);
                }
            }
        });
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (dirsCache.size() >= 1) {
                    if (!dirsCache.isEmpty()) {
                        String currentPath;
                        File newFolder;
                        CreateNewFolderJDialog newFolderJDialog = new CreateNewFolderJDialog(UI.this);

                        if (newFolderJDialog.getReady()) {
                            currentPath = toFullPath(dirsCache);
                            newFolder = new File(currentPath, newFolderJDialog.getNewName());
                            if (!newFolder.exists())
                                newFolder.mkdir();

                            File updateDir = new File(currentPath);
                            String updateMas[] = updateDir.list();
                            DefaultListModel updateModel = new DefaultListModel();
                            for (String str : updateMas) {
                                File check = new File(updateDir.getPath(), str);
                                if (!check.isHidden()) {
                                    if (check.isDirectory()) {
                                        updateModel.addElement(str);
                                    } else {
                                        updateModel.addElement("файл - " + str + " его размер - " + check.length());
                                    }
                                }
                            }
                            filesList.setModel(updateModel);
                        }
                    }
                }
            }
        });
        addFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!dirsCache.isEmpty()){
                    String currentPath;
                    File newFile;
                    CreateNewFileJDialog newFileJDialog = new CreateNewFileJDialog(UI.this);

                    if(newFileJDialog.getReady()){
                        currentPath = toFullPath(dirsCache);
                        newFile = new File(currentPath,newFileJDialog.getNewName());
                        if(!newFile.exists()) {
                            try {
                                newFile.createNewFile();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }

                        File updateDir = new File (currentPath);
                        String updateMas[] = updateDir.list();
                        DefaultListModel updateModel = new DefaultListModel();
                        for (String str : updateMas){
                            File check = new File (updateDir.getPath(), str);
                            if(!check.isHidden()){
                                if(check.isDirectory()){
                                    updateModel.addElement(str);
                                }else{
                                    updateModel.addElement("файл - "+str+ " его размер - "+ check.length());
                                }
                            }
                        }
                        filesList.setModel(updateModel);
                    }
                }
            }
        });
        delButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (dirsCache.size() >= 1) {
                    String selectedObject = filesList.getSelectedValue().toString();
                    String currentPath = toFullPath(dirsCache);
                    if (!selectedObject.isEmpty()) {
                        File select = new File(currentPath, selectedObject);
                        if (select.isDirectory()) {
                            deleteAll(select);
                        } else {
                            deleteAll(new File(currentPath, toNameFile(selectedObject)));
                        }
                        File updateDir = new File(currentPath);
                        String updateMas[] = updateDir.list();
                        DefaultListModel updateModel = new DefaultListModel();
                        for (String str : updateMas) {
                            File check = new File(updateDir.getPath(), str);
                            if (!check.isHidden()) {
                                if (check.isDirectory()) {
                                    updateModel.addElement(str);
                                } else {
                                    updateModel.addElement("файл - " + str + " его размер - " + check.length());
                                }
                            }
                        }
                        filesList.setModel(updateModel);
                    }
                }
            }
        });
        renameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!dirsCache.isEmpty() & filesList.getSelectedValue()!=null){
                    String currentPath = toFullPath(dirsCache);
                    String selectedObject = filesList.getSelectedValue().toString();
                    RenameJDialog renamer = new RenameJDialog(UI.this);
                    if (renamer.getReady()){
                        File renameFile = new File(currentPath,selectedObject);
                        if (!renameFile.isDirectory()){
                           renameFile = new File(currentPath,toNameFile(selectedObject));
                        }
                        renameFile.renameTo(new File(currentPath, renamer.getNewName()));
                        File updateDir = new File (currentPath);
                        String updateMas[] = updateDir.list();
                        DefaultListModel updateModel = new DefaultListModel();
                        for (String str : updateMas){
                            File check = new File (updateDir.getPath(), str);
                            if(!check.isHidden()){
                                if(check.isDirectory()){
                                    updateModel.addElement(str);
                                }else{
                                    updateModel.addElement("файл - "+str+ " его размер - "+ check.length());
                                }
                            }
                        }
                        filesList.setModel(updateModel);
                    }
                }
            }
        });
        reFreash.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (dirsCache.size() >= 1) {
                    String fullPath = toFullPath(dirsCache);
                    File updateDir = new File(fullPath);
                    String updateMas[] = updateDir.list();
                    DefaultListModel updateModel = new DefaultListModel();
                    for (String str : updateMas) {
                        File check = new File(updateDir.getPath(), str);
                        if (!check.isHidden()) {
                            if (check.isDirectory()) {
                                updateModel.addElement(str);
                            } else {
                                updateModel.addElement("файл - " + str + " его размер - " + check.length());
                            }
                        }
                    }
                    filesList.setModel(updateModel);
                }
            }
        });

        buttonsPanel.add(backButton);
        buttonsPanel.add(addButton);
        buttonsPanel.add(addFileButton);
        buttonsPanel.add(delButton);
        buttonsPanel.add(renameButton);
        buttonsPanel.add(reFreash);

        catalogPanel.setLayout(new BorderLayout());
        catalogPanel.add(filesScroll, BorderLayout.CENTER);
        catalogPanel.add(buttonsPanel, BorderLayout.SOUTH);

        getContentPane().add(catalogPanel);
        setSize(600,800);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public String toFullPath (List<String> file){
        String listPart = "";
        for (String str : file){
            listPart = listPart+str+"\\";
        }
        return listPart;
    }
    public void deleteAll (File file){
        File[] objects = file.listFiles();
        if (objects!=null){
            for (File f: objects) {
                deleteAll(f);
            }
        }
        file.delete();
    }
    public String toNameFile (String str){
        String currentPath = toFullPath(dirsCache);
        for (int i=7; i<35;i++){
            File select = new File(currentPath,str.substring(7,i));
            if (select.isFile())
                return str.substring(7,i);
        }
        return null;
    }
}
