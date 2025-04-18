//
//  QRReaderView.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 03/05/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import QRCodeReader
import Cartography
import SwiftyJSON
import AVFoundation

class QRReaderView: UIView, QRCodeReaderDisplayable {
    let cameraView: UIView            = UIView()
    let cancelButton: UIButton?       = UIButton()
    let switchCameraButton: UIButton? = nil
    let toggleTorchButton: UIButton?  = nil
    let overlayView: UIView?          = UIView()
    
    var useBlock: (Bool) -> () = {_ in}
    var infoBlock: (QRCodeBike, @escaping (QRResult) -> ()) -> () = { _, _ in }
    
    let textView: UITextView = {
        let view = UITextView()
        view.isEditable = false
        view.textAlignment = .center
        view.backgroundColor = .clear
        return view
    }()
    
    fileprivate var qrValue: String?
    fileprivate let bottomContainer   = UIView()
    fileprivate let imageView: UIImageView = {
        let view = UIImageView()
        view.contentMode = .scaleAspectFit
        return view
    }()
    
    fileprivate let bikeLabel: UILabel = {
        let label = UILabel()
        label.font = UIFont.boldSystemFont(ofSize: 14)
        label.textColor = .white
        return label
    }()
    
    fileprivate let priceLabel: UILabel = {
        let label = UILabel()
        label.font = UIFont.systemFont(ofSize: 14)
        label.textColor = UIColor(white: 1.0, alpha: 0.7)
        return label
    }()
    
    fileprivate let mapButton: UIButton = {
        let button = UIButton()
        button.setTitle("qr_scanner_map_button".localized(), for: .normal)
        button.backgroundColor = .lsTurquoiseBlue
        return button
    }()
    
    fileprivate let unlockButton: UIButton = {
        let button = UIButton()
        button.setTitle("qr_scanner_unlock_button".localized(), for: .normal)
        button.backgroundColor = .lsTurquoiseBlue
        return button
    }()
    
    fileprivate let errorTitleLabel: UILabel = {
        let label = UILabel()
        label.font = UIFont.boldSystemFont(ofSize: 16)
        label.textColor = .white
        label.text = "route_to_bike_booked_alert_title".localized()
        label.textAlignment = .center
        return label
    }()
    
    fileprivate let errorLabel: UILabel = {
        let label = UILabel()
        label.font = UIFont.systemFont(ofSize: 14)
        label.textColor = .white
        label.text = "route_to_bike_booked_alert_text".localized()
        label.textAlignment = .center
        label.numberOfLines = 0
        return label
    }()
    
    fileprivate let loadingView = UIView()
    
//    fileprivate let label: String
//    init(label: String = "qr_scanner_map_button".localized()) {
//        self.label = label
//        super.init(frame: .zero)
//    }
//    
//    required init?(coder aDecoder: NSCoder) {
//        fatalError("init(coder:) has not been implemented")
//    }
    
    func setupComponents(showCancelButton: Bool, showSwitchCameraButton: Bool, showTorchButton: Bool, showOverlayView: Bool) {
        addSubview(cameraView)
        constrain(cameraView) { (view) in
            view.edges == view.superview!.edges
        }
        
        addSubview(overlayView!)
        constrain(overlayView!) { (view) in
            view.edges == view.superview!.edges
        }
        
        overlayView?.addSubview(bottomContainer)
        constrain(bottomContainer) { (view) in
            view.height == 128
            view.bottom == view.superview!.bottom + 128
            view.left == view.superview!.left
            view.right == view.superview!.right
        }
        
        let blurView = UIVisualEffectView(effect: UIBlurEffect(style: .dark))
        bottomContainer.addSubview(blurView)
        constrain(blurView) { (view) in
            view.edges == view.superview!.edges
        }
        
        bottomContainer.addSubview(loadingView)
        loadingView.backgroundColor = UIColor(white: 0.7, alpha: 0.1)
        loadingView.alpha = 0
        loadingView.frame = bottomContainer.bounds
        
        bottomContainer.addSubview(mapButton)
        bottomContainer.addSubview(unlockButton)
        mapButton.alpha = 0
        unlockButton.alpha = 0
        constrain(mapButton, unlockButton) { (map, unlock) in
            map.height == 54
            map.bottom == map.superview!.bottom
            map.width == unlock.width
            map.left == map.superview!.left
            unlock.height == 54
            unlock.bottom == unlock.superview!.bottom
            unlock.left == map.right
            unlock.right == unlock.superview!.right
        }
        mapButton.addTarget(self, action: #selector(useAction(sender:)), for: .touchUpInside)
        unlockButton.addTarget(self, action: #selector(useAction(sender:)), for: .touchUpInside)
        
        bottomContainer.addSubview(errorTitleLabel)
        bottomContainer.addSubview(errorLabel)
        constrain(errorTitleLabel, errorLabel) { (title, text) in
            text.bottom == text.superview!.bottom - 16
            text.left == text.superview!.left + 16
            text.right == text.superview!.right - 16
            text.height == 34
            
            title.bottom == text.top - 8
            title.left == text.left
            title.right == text.right
            title.height == 20
        }
        
        bottomContainer.addSubview(imageView)
        imageView.image = #imageLiteral(resourceName: "icon_qr_scanner")
        constrain(imageView) { view in
            view.top == view.superview!.top + 8
            view.height == 32
            view.width == 32
            view.left == view.superview!.left + 16
        }
        
        bottomContainer.addSubview(textView)
        constrain(textView, imageView, mapButton) { text, image, button in
            text.top == image.bottom
            text.bottom == button.top - 10
            text.left == image.left
            text.right == text.superview!.right - 16
        }
        
        bottomContainer.addSubview(bikeLabel)
        constrain(bikeLabel, imageView) { (label, image) in
            label.top == label.superview!.top + 8
            label.left == image.right + 8
            label.height == 16
            label.right == label.superview!.right + 16
        }
        
        bottomContainer.addSubview(priceLabel)
        constrain(priceLabel, imageView) { (label, image) in
            label.bottom == image.bottom
            label.left == image.right + 8
            label.height == 16
            label.right == label.superview!.right + 16
        }
        
        addSubview(cancelButton!)
        cancelButton?.setTitle(nil, for: .normal)
        constrain(cancelButton!, self) { (view, superView) in
            view.top == superView.top + 20
            view.left == superView.left + 10
            view.width == 60
            view.height == 40
        }
    }
    
    func display(result: QRCodeReaderResult?) {
        guard qrValue != result?.value else { return }
        qrValue = result?.value
        
        bikeLabel.text = result?.bike?.display
        imageView.image = #imageLiteral(resourceName: "icon_qr_scanner")
        priceLabel.text = nil
        textView.text = nil
        
        constrain(bottomContainer) { view in
            view.bottom == view.superview!.bottom + 64
        }
        
        UIView.animate(withDuration: .defaultAnimation) {
            self.mapButton.alpha = 0
            self.unlockButton.alpha = 0
            self.errorLabel.alpha = 0
            self.errorTitleLabel.alpha = 0
            self.layoutIfNeeded()
        }
        
        guard let qr = result?.bike else { return }
        startLoading()
        infoBlock(qr) { [weak self] res in
            self?.stopLoading()
            switch res {
            case .success(let bike):
                self?.show(bike: bike)
            case .fail(let error):
                self?.show(error: error)
            }
        }
    }
    
    fileprivate func show(bike: Bike) {
        bikeLabel.text = "\(bike.name!): \(bike.fleetName!)"
        if let logo = bike.fleetLogo, let url = URL(string: logo) {
            imageView.sd_setImage(with: url)
        }
        
        if bike.fleetType == .privatePay || bike.fleetType == .publicPay, let price = bike.priceForMembership, let duration = bike.priceDuration, let unit = bike.priceUnit, duration > 0, price > 0 {
            priceLabel.text = price.priceValue! + String(format: "payment_bike_card_per".localized(), "\(duration)", unit)
        } else {
            priceLabel.text = "payment_cost_free".localized()
        }
        
        if let terms = bike.termsLink {
            let text = NSMutableAttributedString(string: "find_rite_terms_body_qr".localized(), attributes: [NSAttributedStringKey.font: UIFont.systemFont(ofSize: 9), NSAttributedStringKey.foregroundColor: UIColor.white])
            let range = (text.string as NSString).range(of: "find_rite_terms_title".localized())
            text.addAttributes([NSAttributedStringKey.link: terms], range: range)
            textView.attributedText = text
            textView.linkTextAttributes = [NSAttributedStringKey.underlineStyle.rawValue: NSUnderlineStyle.styleSingle.rawValue,
                                           NSAttributedStringKey.foregroundColor.rawValue: UIColor.white,
                                           NSAttributedStringKey.underlineColor.rawValue: UIColor.white]
        }
        
        constrain(bottomContainer) { view in
            view.bottom == view.superview!.bottom
        }
        UIView.animate(withDuration: .defaultAnimation) {
            self.mapButton.alpha = 1
            self.unlockButton.alpha = 1
            self.errorLabel.alpha = 0
            self.errorTitleLabel.alpha = 0
            self.layoutIfNeeded()
        }
    }
    
    fileprivate func show(error: String) {
        errorLabel.text = error
        constrain(bottomContainer) { view in
            view.bottom == view.superview!.bottom
        }
        UIView.animate(withDuration: .defaultAnimation) {
            self.mapButton.alpha = 0
            self.unlockButton.alpha = 0
            self.errorLabel.alpha = 1
            self.errorTitleLabel.alpha = 1
            self.layoutIfNeeded()
        }
    }
    
    fileprivate func startLoading() {
        UIView.animate(withDuration: .defaultAnimation) { 
            self.loadingView.alpha = 1
        }
        loadingView.frame = {
            var frame = bottomContainer.bounds
            frame.origin.x = -frame.width
            return frame
        }()
        
        UIView.animate(withDuration: 2, delay: 0, options: [.curveEaseInOut, .repeat], animations: {
            self.loadingView.frame = {
                var frame = self.bottomContainer.bounds
                frame.origin.x = frame.width
                return frame
            }()
        }, completion: nil)
    }
    
    fileprivate func stopLoading() {
        constrain(self.bottomContainer) { view in
            view.bottom == view.superview!.bottom + 128
            view.left == view.superview!.left
            view.right == view.superview!.right
            view.height == 128
        }
        UIView.animate(withDuration: .defaultAnimation, animations: {
            self.loadingView.alpha = 0
            self.layoutIfNeeded()
        }, completion: { _ in
            self.loadingView.layer.removeAllAnimations()
        })
    }
    
    fileprivate func hideBike() {
        constrain(bottomContainer) { view in
            view.bottom == view.superview!.bottom + 64
        }
        UIView.animate(withDuration: .defaultAnimation) {
            self.layoutIfNeeded()
        }
    }
    
    @objc private func useAction(sender: UIButton) {
        useBlock(sender == unlockButton)
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        loadingView.frame = bottomContainer.bounds
    }
}
