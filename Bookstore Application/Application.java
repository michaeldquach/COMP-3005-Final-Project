import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Application {
    static Bookstore bookstore;

    public static void launchApplication(boolean owner){            
        if(bookstore != null){
            bookstore.close();
        }
        if(owner){
            bookstore = new OwnerBookstore();
            bookstore.initializeView();
        }
        else{
            bookstore = new CustomerBookstore();
            bookstore.initializeView();
        }
    }
    public static void main(String args[]){        
        JFrame frame = new JFrame("Look Inna Book - Select Interface");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 200);

        JPanel mainPane = new JPanel(new GridLayout(1, 2));
        mainPane.setSize(600, 200);
        frame.add(mainPane);

        JButton launchUserApplicationButton = new JButton("Launch Customer Bookstore Application");
        mainPane.add(launchUserApplicationButton);
        launchUserApplicationButton.addActionListener((ActionListener) new ActionListener(){  
            @Override
            public void actionPerformed(ActionEvent e) {      
                launchApplication(false);
            }  
        }); 

        JButton launchOwnerApplicationButton = new JButton("Launch Owner Bookstore Application");
        mainPane.add(launchOwnerApplicationButton);
        launchOwnerApplicationButton.addActionListener((ActionListener) new ActionListener(){  
            @Override
            public void actionPerformed(ActionEvent e) {       
                launchApplication(true);
            }  
        }); 

        frame.setVisible(true);
    }    
}
