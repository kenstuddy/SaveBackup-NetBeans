package org.ken.SaveBackup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.spi.editor.document.OnSaveTask;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 * This class handles the saving of files. It implements the OnSaveTask interface and
 * overrides (implements) the methods performTask, runLocked, and cancel of the interface OnSaveTask.
 * It also handles MIME (Multipurpose Internet Mail Extension) registration of all file types and overrides (implements) the
 * method createTask of the OnSaveTask Factory interface.
 * @author Ken Studdy
 * @date November 3, 2018
 * @version 1.0
 */
public class SaveBackup implements OnSaveTask {
    
    private final Document doc;
    private final boolean debugMode = false;
    private String src;
    private String dest;
    private String logTime;
    
   /**
    * Private SaveBackup constructor that takes a parameter of type Document.
    * @param document The current document
    */
    private SaveBackup(Document document) {
        this.doc = document;
    }
    
   /**
    * Override the performTask method of the OnSaveTask interface.
    */
    @Override
    public void performTask() {
        //Create a new DataObject based on the stream used to initialize the document.
        DataObject data = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
        //Create a new FileObject based on the primary file of the Data Object.
        FileObject file = data.getPrimaryFile();
        
        //This is actually the full name and path of the file, not just the path.
        src = file.getPath();
        
        //The destination folder should be in the user's home directory, this works on most operating systems and prevents permission issues.
        dest = System.getProperty("user.home") + File.separator +  ".SaveBackup";

        //If we are on Windows, we need to append another file separator to the end of our destination folder.
        if (System.getProperty("os.name").startsWith("Windows")) {
            dest += File.separator;
        }
        
        //On Windows, we cannot have : in the folder name, and we might as well remove it for other operating systems too for cross-platform compatibility.
        dest += src.replace(":", "");
        
        //Here is the date and time that the file is saved at.
        logTime = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Calendar.getInstance().getTime());
        
        //Since the destination path (the output file) originally contains the current file name without the date and time, we must append the date and time to the filename.
        dest = dest.replace("." + file.getExt(), "-" + logTime + "." + file.getExt());
        
        try {
            //We don't actually do anything with this file other than use it for creating parent folders if they don't already exist.
            File newFile = new File(dest);

            //Create all the folders for the output directory if they don't exist, this also works with Linux by incrementally creating the folders one at a time.
            if (!newFile.getParentFile().exists()) {
                newFile.getParentFile().mkdirs();
            }
            //Here we call our static copy wrapper method and pass two stings to it. This creates the destination file by copying the source file to the destination name.
            copy(src, dest);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        //This if statement is used for debugging, we can put any logging code here.
        if (debugMode) {
            Logger.getLogger(SaveBackup.class.getName()).info("=======> Saving " + file.getPath()+ " <=======");
        }
        
    }
    
    /**
     * This is a wrapper for the copy method of the Java Files class. The Files class has been available since Java SE 7,
     * so it makes sense to make use of it instead of older ways of copying files.
     * @param sourcePath
     * @param destinationPath
     * @throws IOException
     */
    public static void copy(String sourcePath, String destinationPath) throws IOException {
        Files.copy(Paths.get(sourcePath), new FileOutputStream(destinationPath));
    }  
    
    /**
     * Override the performTask method of the OnSaveTask interface, this takes a parameter of type runnable.
     * @param r The Runnable interface
     */
    @Override
    public void runLocked(Runnable r) {
        r.run();
    }

    /**
     * Override the performTask method of the OnSaveTask interface.
     * @return true This returns true to indicate that the OnSaveTask task was cancelled successfully.
     */
    @Override
    public boolean cancel() {
        return true;
    }
    
    /**
     * This annotation creates a mime registration for all file types and uses the OnSaveTask Factory service.
     */
    @MimeRegistration(mimeType = "", service = OnSaveTask.Factory.class)
    public static final class Factory implements OnSaveTask.Factory {
        
        /**
         * Override the createTask method in the OnSaveTask Factory interface.
         * @param context
         * @return object This returns an instance of the SaveBackup class when the task is created.
         */
        @Override
        public OnSaveTask createTask(OnSaveTask.Context context) {
            return new SaveBackup(context.getDocument());
        }  
    }
}
