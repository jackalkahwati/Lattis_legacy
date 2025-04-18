//
//  SLShareInstructionViewController.swift
//  Ellipse
//
//  Created by Ranjitha on 11/19/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import UIKit

class SLShareInstructionViewController: UIViewController {
    var shareView:UIView!
    var inviteButton:UIButton!
    var closeButton:UIButton!
    var image:UIImage!
    
    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
        shareView = UIView()
        shareView.frame = self.view.frame
        shareView.backgroundColor = UIColor.white
        
        let image = UIImage(named: "close_icon") as UIImage?
        closeButton = UIButton()
        closeButton = UIButton(frame: CGRect(x: self.view.frame.size.width - 50,
                                             y: 35, width: 25 ,
                                             height: 25))
        closeButton.setImage(image, for: .normal)
        closeButton.addTarget(self, action:#selector(closePress), for: UIControlEvents.touchUpInside)
       // closeButton .imageView? .contentMode = UIViewContentMode.redraw
        shareView.addSubview(closeButton)
        
        let label = UILabel()
        let size:CGSize = self.view.frame.size
        label.center = CGPoint(x: size.width/2,y: size.width/2)
        label.frame = CGRect(x:26,
                             y:closeButton.frame.origin.y + 80,
                             width:size.width - 52,height: 20)
        label.textAlignment = NSTextAlignment.center
        label.font = UIFont(name: SLFont.OpenSansRegular.rawValue, size: 14.0)
        label.textColor =  UIColor.color(72, green: 216, blue: 255)
        label.lineBreakMode = NSLineBreakMode.byWordWrapping
        label.translatesAutoresizingMaskIntoConstraints = false
        label.text="HOW DO I SHARE MY ELLIPSE?"
        shareView.addSubview(label)
        
        let text = UITextView()
        text.frame = CGRect(x:26 ,
                            y:label.frame.origin.y + 30,
                            width:size.width - 52,
                            height:100)
        text.textAlignment = NSTextAlignment.center
        text.font = UIFont(name: SLFont.OpenSansRegular.rawValue, size: 14.0)
        text.textColor = UIColor.color(155, green: 155, blue: 155)
        text.textContainer.lineBreakMode = NSLineBreakMode.byWordWrapping
        text.text = "You can share your Ellipse with any of your phone contacts.  Just choose a contact and we’ll SMS them an invitation."
        text.isUserInteractionEnabled = false
        shareView.addSubview(text)
        
        let label1 = UILabel()
        label1.frame = CGRect(x:26,
                              y:text.frame.origin.y + 120,
                              width: size.width - 52,height: 20)
        label1.textAlignment = .center
        label1.font = UIFont(name: SLFont.OpenSansRegular.rawValue, size: 14.0)
        label1.textColor = UIColor.color(72, green: 216, blue: 255)
        label1.lineBreakMode = NSLineBreakMode.byWordWrapping;
        label1.text = "WHAT DO MY FRIENDS NEED TO DO?"
        shareView.addSubview(label1)
        
        let text1 = UITextView()
        text1.frame = CGRect(x:26 ,
                             y:label1.frame.origin.y + 30,
                             width:size.width - 52,
                             height: 450)
        text1.textAlignment = NSTextAlignment.center
        text1.font = UIFont(name: SLFont.OpenSansRegular.rawValue, size: 14.0)
        text1.textColor = UIColor.color(155, green: 155, blue: 155)
        text1.textContainer.lineBreakMode = NSLineBreakMode.byWordWrapping
        text1.text = "They’ll need to install the Ellipse app and once they’ve accepted your invitation, they can start using your Ellipse.  They’ll be able to lock and unlock it just like you can but they won’t be able to change any of your settings."
        text1.isUserInteractionEnabled = false
        shareView.addSubview(text1)
        
        inviteButton = UIButton()
        inviteButton = UIButton(frame: CGRect(x: 26,
                                              y:self.view.frame.size.height - 80,width: size.width - 52 , height: 50))
        inviteButton.backgroundColor = UIColor.color(87, green: 216, blue: 255)
        inviteButton.setTitle("OK, GOT IT", for: .normal)
        inviteButton.addTarget(self, action:#selector(sendBtn), for: UIControlEvents.touchUpInside)
        shareView.addSubview(inviteButton)
        self.view.addSubview(shareView)
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func closePress(sender: UIButton) {
       // shareView.isHidden = true
        
        if let navController = self.navigationController {
            navController.dismiss(animated: true, completion: nil)
        } else {
            self.dismiss(animated: true, completion: nil)
        }
    }
    
    func sendBtn(sender: UIButton){
        let slvc = SLSharingViewController()
        self.navigationController?.setViewControllers([slvc], animated: true)
    }
    override func viewWillAppear(_ animated: Bool) {
        self.navigationController?.isNavigationBarHidden = true
    }
    override func viewWillDisappear(_ animated: Bool) {
        self.navigationController?.isNavigationBarHidden = false

    }
}
