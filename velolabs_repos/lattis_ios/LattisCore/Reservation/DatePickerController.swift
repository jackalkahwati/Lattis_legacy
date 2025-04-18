//
//  DatePickerController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 14.08.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import UIKit
import Cartography

class DatePickerController: UIViewController {
    
    fileprivate let cardView = UIView()
    fileprivate let pickerView = UIDatePicker()
    fileprivate let titleLabel = UILabel.label(font: .theme(weight: .bold, size: .small))
    fileprivate let subtitleLabel = UILabel.label(text: "select_date_time".localized(), font: .theme(weight: .bold, size: .title))
    fileprivate let closeButton = UIButton(type: .system)
    fileprivate let doneButton = ActionButton()
    fileprivate let completion: (Date) -> ()
    
    init(title: String, date: Date?, min: Date, max: Date, interval: Int = 30, completion: @escaping (Date) -> ()) {
        self.titleLabel.text = title
        self.completion = completion
        super.init(nibName: nil, bundle: nil)
        modalPresentationStyle = .overCurrentContext
        pickerView.date = date ?? min
        pickerView.minimumDate = min
        pickerView.maximumDate = max
        pickerView.minuteInterval = interval
        if #available(iOS 13.4, *) {
            pickerView.preferredDatePickerStyle = .wheels
        }
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        if #available(iOS 13.0, *) {
            overrideUserInterfaceStyle = .light
        }
        
        view.backgroundColor = UIColor(white: 0.3, alpha: 0.8)
        
        cardView.backgroundColor = .white
        cardView.layer.maskedCorners = [.layerMinXMinYCorner, .layerMaxXMinYCorner]
        cardView.layer.cornerRadius = .containerCornerRadius
        
        view.addSubview(cardView)
        cardView.addSubview(closeButton)
        cardView.addSubview(titleLabel)
        cardView.addSubview(subtitleLabel)
        cardView.addSubview(pickerView)
        cardView.addSubview(doneButton)
        
        closeButton.setImage(.named("icon_close"), for: .normal)
        closeButton.tintColor = .black
        
        constrain(cardView, closeButton, titleLabel, subtitleLabel, pickerView, doneButton, view) { card, close, title, subtitle, picker, done, view in
            card.bottom == view.bottom
            card.left == view.left
            card.right == view.right
            
            done.bottom == view.safeAreaLayoutGuide.bottom - .margin
            done.left == card.left + .margin
            done.right == card.right - .margin
            
            picker.bottom == done.top
            picker.left == card.left
            picker.right == card.right
            
            subtitle.bottom == picker.top
            subtitle.left == card.left + .margin
            subtitle.right == card.right - .margin
            
            title.left == subtitle.left
            title.right == subtitle.right
            title.bottom == subtitle.top
            
            close.bottom == title.top
            close.right == card.right - .margin
            close.top == card.top + .margin
        }
        
        doneButton.action = .plain(title: "done".localized(), handler: done)
        closeButton.addTarget(self, action: #selector(close), for: .touchUpInside)
    }
    
    fileprivate func done() {
        completion(pickerView.date)
        close()
    }
    
    fileprivate func present() {
        view.layoutIfNeeded()
        UIView.animate(withDuration: 0.3, animations: {
            self.view.layoutIfNeeded()
        }, completion: { _ in
            
        })
    }
    
    @objc
    internal override func close() {
        dismiss(animated: true, completion: nil)
    }
}
