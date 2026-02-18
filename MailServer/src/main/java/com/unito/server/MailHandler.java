package com.unito.server;

import com.unito.common.Mail;
import com.unito.server.DataModel.ServerModel;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Semaphore;

public class MailHandler {
    private static List<Mail> mails = new ArrayList<>();
    private static String pathname = "C:\\Users\\budda\\Documents\\PROG3\\MailParent\\MailServer\\src\\main\\resources\\data\\";

    private static final Map<String, Semaphore> userSemaphores = new HashMap<>();

    private static Semaphore getUserSemaphore(String username) {
        synchronized (userSemaphores) {
            return userSemaphores.computeIfAbsent(username, u -> new Semaphore(1));  // Se esiste già un semaforo associato a un determinato username, viene semplicemente restituito quel semaforo. Se non esiste, viene creato un nuovo semaforo con un solo permesso (new Semaphore(1)), lo si associa alla chiave username nella mappa, e poi viene restituito.
        }
    }

    public static List<Mail> importEmails(String userAddress) {
        mails.clear();
        List<List<String>> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(pathname+userAddress+".csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split("-\\|-");
                records.add(Arrays.asList(values));
                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                Date date = dateFormat.parse(values[5]);
                if (values[4].startsWith("- Messaggio inoltrato -")) {
                    // Modifica della stringa per formattarla come richiesto
                    String formattedMessage = values[4].replaceFirst("From:", "\nFrom:");
                    formattedMessage = formattedMessage.replace("Date:", "\nDate:")
                            .replace("Object:", "\nObject:")
                            .replace("To:", "\nTo:")
                            .replace("Content:", "\nContent: ")
                            .replace(" --- ", "\n --- ");
                    
                    // Crea la Mail con il messaggio formattato
                    mails.add(new Mail(Integer.parseInt(values[0]), values[1], values[2], values[3], formattedMessage, date));
                } else {
                    // Se non è "Messaggio inoltrato", usa il messaggio originale
                    mails.add(new Mail(Integer.parseInt(values[0]), values[1], values[2], values[3], values[4], date));
                }
            }
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
        return mails;
    }

    public static synchronized Integer getLastID(String userMail){
        mails.clear();
        int id = 1;
        try (BufferedReader br = new BufferedReader(new FileReader(pathname+userMail+".csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split("-\\|-");
                id = Integer.parseInt(values[0]);
            }
            id++;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return id;
    }

    public static Boolean saveMails(Mail email) {
        String[] to = email.getTo().split(";");
        for(String address : to){
            if(ServerModel.getRegisteredUsers().contains(address)){
                Semaphore userSemaphore = getUserSemaphore(address);
                try{
                    userSemaphore.acquire();
                    try(PrintWriter pw = new PrintWriter(new FileWriter(pathname + address + ".csv", true))){
                        int id = getLastID(address);
                        Date date = new Date();
                        String str = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(date);
                        pw.append(Integer.toString(getLastID(address)));
                        pw.append("-|-");
                        pw.append(email.getFrom());
                        pw.append("-|-");
                        pw.append(email.getTo());
                        pw.append("-|-");
                        pw.append(email.getObject());
                        pw.append("-|-");
                        pw.append(email.getContentNoLineBreaks());
                        pw.append("-|-");
                        pw.append(str);
                        pw.append("\n");
                    }catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }catch(InterruptedException e){
                    Thread.currentThread().interrupt();
                    System.err.println("Thread interrotto durante l'attesa del semaforo");
                }finally {
                    userSemaphore.release();
                }
            }
            else {
                return false;
            }
        }
        return true;
    }

    public static boolean removeMail(String user, int id) {
        boolean result = false;
        File inputFile = new File(pathname + user + ".csv");
        File tempFile = new File(pathname + user + "_temp.csv"); // temp unico per utente
        Semaphore userSemaphore = getUserSemaphore(user);

        try {
            userSemaphore.acquire();
            try (
                    BufferedReader br = new BufferedReader(new FileReader(inputFile));
                    PrintWriter pw = new PrintWriter(new FileWriter(tempFile))
            ) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] value = line.split("-\\|-");
                    if (!value[0].equals(Integer.toString(id))) {
                        pw.println(line);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            boolean deleted = inputFile.delete();
            boolean renamed = deleted && tempFile.renameTo(inputFile);
            result = deleted && renamed;

            // Debug info
            System.out.println("Tentativo rimozione mail ID: " + id + " per utente: " + user);
            System.out.println("File cancellato: " + deleted);
            System.out.println("File rinominato: " + renamed);
            System.out.println("Rimozione riuscita? " + result);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Thread interrotto durante attesa semaforo");
        } finally {
            userSemaphore.release();
        }

        return result;
    }

}
