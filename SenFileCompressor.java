/*
 * Projet réalisé par : 
 * - Nom : Dienaba Fily Bodiang et Adja Rohaya Fall
 */
import java.io.*;
import java.util.zip.*;

public class SenFileCompressor {

    public static void main(String[] args) {
        if (args.length < 1 || args[0].equals("-h")) {
            afficherAide();
            return;
        }

        boolean compresser = false;
        boolean decompresser = false;
        String fichierCompresse = null;
        String repertoireDestination = null;
        boolean creerRepertoire = false;
        boolean verbose = false;

        // Analyser les arguments de la ligne de commande
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-c":
                    compresser = true;
                    break;
                case "-d":
                    decompresser = true;
                    break;
                case "-r":
                    if (i + 1 < args.length) {
                        repertoireDestination = args[i + 1];
                        i++;
                    }
                    break;
                case "-f":
                    creerRepertoire = true;
                    break;
                case "-v":
                    verbose = true;
                    break;
                default:
                    if (compresser && fichierCompresse == null) {
                        fichierCompresse = args[i];
                    } else if (decompresser && fichierCompresse == null) {
                        fichierCompresse = args[i];
                    }
                    break;
            }
        }

        // Exécuter l'action appropriée
        if (compresser) {
            if (fichierCompresse != null) {
                compresserFichiers(fichierCompresse.split("\\s+"), repertoireDestination, creerRepertoire, verbose);
            } else {
                System.out.println("Fichiers à compresser manquants.");
                afficherAide();
            }
        } else if (decompresser) {
            if (fichierCompresse != null) {
                decompresserFichier(fichierCompresse, repertoireDestination, creerRepertoire, verbose);
            } else {
                System.out.println("Fichier à décompresser manquant.");
                afficherAide();
            }
        } else {
            System.out.println("Option invalide. Utilisez '-h' pour obtenir de l'aide.");
            afficherAide();
        }
    }

    private static void afficherAide() {
        System.out.println("Utilisation :");
        System.out.println("java SenFileCompressor -h");
        System.out.println("java SenFileCompressor -c <fichier1> <fichier2> ...");
        System.out.println("java SenFileCompressor -d <fichierCompresse.sfc>");
        System.out.println("\nOptions :");
        System.out.println("-r <repertoire> : Spécifie le répertoire de destination.");
        System.out.println("-f : Crée le répertoire de destination s'il n'existe pas.");
        System.out.println("-v : Active le mode verbeux (affiche des informations détaillées).");
    }

    private static void compresserFichiers(String[] fichiersACompresser, String repertoireDestination, boolean creerRepertoire, boolean verbose) {
        try {
            File tempDir = new File("temp");
            if (!tempDir.exists()) {
                tempDir.mkdir();
            }
    
            for (String fichier : fichiersACompresser) {
                File sourceFile = new File(fichier);
                File destFile = new File(tempDir.getAbsolutePath() + File.separator + sourceFile.getName());
                copyFile(sourceFile, destFile);
            }
    
            try (FileOutputStream fos = new FileOutputStream(repertoireDestination + File.separator + "archive.sfc");
                 ZipOutputStream zos = new ZipOutputStream(fos)) {
                zipDirectory(tempDir, zos, tempDir.getAbsolutePath().length() + 1);
            }
    
            deleteDirectory(tempDir);
    
            System.out.println("Compression réussie. Archive créée : " + repertoireDestination + File.separator + "archive.sfc");
        } catch (IOException e) {
            System.out.println("Échec de la compression : " + e.getMessage());
        }
    }   
    

    private static void decompresserFichier(String fichierCompresse, String repertoireDestination, boolean creerRepertoire, boolean verbose) {
        String destination = repertoireDestination != null ? repertoireDestination : "répertoire courant";
        try (FileInputStream fis = new FileInputStream(fichierCompresse);
             ZipInputStream zis = new ZipInputStream(fis)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String fileName = entry.getName();
                File outputFile = new File(repertoireDestination + File.separator + fileName);
                File parentDir = outputFile.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }
                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = zis.read(buffer)) != -1) {
                        fos.write(buffer, 0, length);
                    }
                }
                zis.closeEntry();
            }
            System.out.println("Décompression réussie. Fichiers extraits dans : " + destination);
        } catch (IOException e) {
            System.out.println("Échec de la décompression : " + e.getMessage());
        }
    }
      

    private static void zipDirectory(File directory, ZipOutputStream zos, int rootLength) throws IOException {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    zipDirectory(file, zos, rootLength);
                } else {
                    String entryName = file.getAbsolutePath().substring(rootLength);
                    ZipEntry entry = new ZipEntry(entryName);
                    zos.putNextEntry(entry);
                    try (FileInputStream fis = new FileInputStream(file)) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = fis.read(buffer)) > 0) {
                            zos.write(buffer, 0, length);
                        }
                    }
                    zos.closeEntry();
                }
            }
        }
    }

    private static void copyFile(File sourceFile, File destFile) throws IOException {
        try (InputStream is = new FileInputStream(sourceFile);
             OutputStream os = new FileOutputStream(destFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        }
    }

    private static void deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        directory.delete();
    }

}
