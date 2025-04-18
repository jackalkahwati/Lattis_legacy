//
//  SLSharingInviteFormViewController.swift
//  Ellipse
//
//  Created by Ranjitha on 10/23/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import UIKit
import Crashlytics

class SLSharingInviteFormViewController: UIViewController {
    let xPadding:CGFloat = 10.0
    
    var isConnected:Bool = true
    var nameLabel: UILabel!
    var descriptionDetail:UILabel!
    var shareView: UIView!
    var closeButton:UIButton!
    var userImage:UIImageView!
    
    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
        let screenHeight = self.view.frame.height
        let screenWidth = self.view.frame.width
        
        shareView = UIView()
        shareView.frame = self.view.frame
        shareView.backgroundColor = UIColor.white
        self.view.backgroundColor = UIColor.white
        self.navigationController?.isNavigationBarHidden = true
      
        shareView.frame = CGRect(x:0,y:0,width:screenWidth,height: screenHeight/2 )
        
        let frame = CGRect(
            x: 0,
            y: 20 ,
            width: 100,
            height: 100
        )

        let image = UIImage(named: "close_icon") as UIImage?
        userImage = UIImageView(image: UIImage(named:"invite_icon"))
        userImage.frame = frame
        let size:CGSize = shareView.frame.size;
        userImage.center = CGPoint(x: size.width/2,
                                   y: size.width/2 - 80)
        self.userImage.center=self.view.center
        shareView.addSubview(userImage)
        
        closeButton = UIButton()
        closeButton = UIButton(frame: CGRect(
            x: self.view.frame.size.width - 50,
            y: 50, width: 20 ,
            height: 20))
        closeButton.setImage(image, for: .normal)
        closeButton.addTarget(self, action:#selector(closePress), for: UIControlEvents.touchUpInside)
        shareView.addSubview(closeButton)
        
        nameLabel = UILabel()
        nameLabel.frame = {
            var frame = shareView.bounds.insetBy(dx: 36, dy: 0)
            frame.origin.y = userImage.frame.maxY + 36
            frame.size.height = nameLabel.font.lineHeight
            return frame
        }()
        nameLabel.textAlignment = .center
        nameLabel.font = .systemFont(ofSize: 20)
        nameLabel.textColor = .slBluegrey
        nameLabel.lineBreakMode = .byWordWrapping;
        nameLabel.text = "Share invitation sent".localized()
        
        shareView.addSubview(nameLabel)
        
        descriptionDetail = UILabel()
        descriptionDetail.textAlignment = .center
        descriptionDetail.font = .systemFont(ofSize: 14)
        descriptionDetail.textColor = .slWarmGreyThree
        descriptionDetail.lineBreakMode = .byWordWrapping
        descriptionDetail.numberOfLines = 0
        descriptionDetail.text = "We’ll notify you when your friend accepts.".localized()
        descriptionDetail.frame = {
            var frame = nameLabel.frame
            frame.origin.y = nameLabel.frame.maxY + 8
            frame.size.height = descriptionDetail.sizeThatFits(CGSize(width: frame.width, height: .greatestFiniteMagnitude)).height
            return frame
        }()
        
        shareView.addSubview(descriptionDetail)
        self.view.addSubview(shareView)
        
        Answers.logShare(withMethod: "Lock shared", contentName: nil, contentType: nil, contentId: nil, customAttributes: nil)
    }
    
    func closePress(sender: UIButton) {
        dismiss(animated: true, completion: nil)
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        self.navigationController?.isNavigationBarHidden = false
    }
}
