//
//  DamageNotesViewController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 19/07/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import Cartography

class DamageNotesViewController: EditViewController {
    
    fileprivate let titleLabel = UILabel()
    fileprivate let textView = UITextView()
    fileprivate var topLayout: NSLayoutConstraint!
    fileprivate var heightLayout: NSLayoutConstraint!
    
    fileprivate let completion: (String?) -> ()
    
    init(_ text: String?, completion: @escaping (String?) -> ()) {
        self.textView.text = text
        self.completion = completion
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        view.addSubview(contentView)
        contentView.addSubview(titleLabel)
        contentView.addSubview(textView)
        
        titleLabel.font = .theme(weight: .bold, size: .small)
        titleLabel.textColor = .darkGray
        titleLabel.text = "Notes"
        titleLabel.setContentHuggingPriority(.defaultHigh, for: .vertical)
        
        textView.font = .theme(weight: .medium, size: .body)
        textView.backgroundColor = UIColor.neonBlue.withAlphaComponent(0.5)
        textView.textColor = .white
        textView.layer.cornerRadius = 5
        textView.isScrollEnabled = false
        
        contentView.layer.cornerRadius = .margin
        contentView.backgroundColor = .white
        
        constrain(contentView, titleLabel, textView, view) { content, title, text, view in
            title.top == content.top + .margin
            title.left == content.left + .margin
            title.right == content.right - .margin
            
            text.top == title.bottom + .margin/4
            text.left == title.left
            text.right == title.right
            self.heightLayout = text.height == 44 ~ .defaultHigh
            
//            action.left == title.left
//            action.right == title.right
//            action.top == text.bottom + .margin
//            action.height == 44
//            action.bottom == content.bottom - .margin
            
            content.left == view.left
            content.right == view.right
            self.topLayout = content.top == view.safeAreaLayoutGuide.top + 64 ~ .defaultLow
            self.centerLayout = content.centerY == view.centerY ~ .defaultHigh
            self.bottomLayout = content.bottom == view.bottom ~ .defaultLow
        }

        textView.textContainerInset = .init(top: .margin/2, left: .margin/4, bottom: .margin/2, right: .margin/4)
    }
    
    override func keyboardWillShow(notification: Notification) {
        topLayout.priority = .defaultHigh
        heightLayout.priority = .defaultLow
        textView.isScrollEnabled = true
        super.keyboardWillShow(notification: notification)
    }
    
    @objc fileprivate func handleAction() {
        completion(textView.text)
        dismiss(animated: true, completion: nil)
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        textView.becomeFirstResponder()
    }
}
