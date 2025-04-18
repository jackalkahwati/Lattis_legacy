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
import AVFoundation

extension UIView: QRCodeReaderViewOverlay {
    public func setState(_ state: QRCodeReaderViewOverlayState) {
    }
}

class QRReaderView: UIView, QRCodeReaderDisplayable {    
    func setNeedsUpdateOrientation() {
        
    }
    
    let cameraView: UIView            = UIView()
    let cancelButton: UIButton?       = UIButton()
    let switchCameraButton: UIButton? = nil
    let toggleTorchButton: UIButton?  = nil
    let overlayView: QRCodeReaderViewOverlay?          = UIView()
    fileprivate let bottomContainer   = UIView()
    fileprivate var multiTimer: Timer?
    fileprivate var cache: QRCodeReaderResult?
    fileprivate weak var reader: QRCodeReader?
    
    var useBlock: () -> () = {}
    
    fileprivate let infoLabel: UILabel = {
        let label = UILabel()
        label.font = UIFont.boldSystemFont(ofSize: 14)
        label.numberOfLines = 0
        label.textColor = .white
        return label
    }()
    
    fileprivate let useButton: UIButton = {
        let button = UIButton()
        button.backgroundColor = .lsTurquoiseBlue
        return button
    }()
    
    fileprivate let flashButton: UIButton = {
        let button = UIButton.init(type: .custom)
        button.setImage(UIImage(named: "flash_off"), for: .normal)
        button.setImage(UIImage(named: "flash_on"), for: .selected)
        return button
    }()
    
    fileprivate let label: String
    init(label: String = "qr_scanner_use_button".localized()) {
        self.label = label
        super.init(frame: .zero)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func setupComponents(with builder: QRCodeReaderViewControllerBuilder) {
        addSubview(cameraView)
        constrain(cameraView) { (view) in
            view.edges == view.superview!.edges
        }
        
        addSubview(overlayView!)
        let v = overlayView! as UIView
        constrain(v) { (view) in
            view.edges == view.superview!.edges
        }
        
        overlayView?.addSubview(bottomContainer)
        constrain(bottomContainer) { (view) in
            view.height == 64
            view.bottom == view.superview!.bottom
            view.left == view.superview!.left
            view.right == view.superview!.right
        }
        
        let blurView = UIVisualEffectView(effect: UIBlurEffect(style: .dark))
        bottomContainer.addSubview(blurView)
        constrain(blurView) { (view) in
            view.edges == view.superview!.edges
        }
        
        bottomContainer.addSubview(useButton)
        useButton.setTitle(label, for: .normal)
        useButton.isHidden = true
        constrain(useButton) { (view) in
            view.top == view.superview!.top
            view.bottom == view.superview!.bottom
            view.width == 100
            view.right == view.superview!.right
        }
        useButton.addTarget(self, action: #selector(useAction), for: .touchUpInside)
        
        bottomContainer.addSubview(infoLabel)
        constrain(infoLabel) { (view) in
            view.top == view.superview!.top
            view.bottom == view.superview!.bottom
            view.left == view.superview!.left + 16
        }
        
        constrain(infoLabel, useButton) { (label, button) in
            label.right == button.left + 16
        }
        
        addSubview(cancelButton!)
        cancelButton?.setTitle(nil, for: .normal)
        constrain(cancelButton!, self) { (view, superView) in
            view.top == superView.safeAreaLayoutGuide.top + 20
            view.left == superView.left + 10
            view.width == 60
            view.height == 40
        }
        
        addSubview(flashButton)
        constrain(flashButton, self) { (view, superview) in
            view.top == superview.safeAreaLayoutGuide.top + 20
            view.right == superview.right - 8
            view.width == 40
            view.height == 40
        }
        flashButton.removeTarget(nil, action: nil, for: .allEvents)
        flashButton.addTarget(self, action: #selector(flash), for: .touchUpInside)
        
        self.reader = builder.reader
        if let reader = reader {
            reader.previewLayer.frame = cameraView.bounds
            cameraView.layer.insertSublayer(reader.previewLayer, at: 0)
        }
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        reader?.previewLayer.frame = cameraView.bounds
    }
    
    func display(result: QRCodeReaderResult?) {
        if let old = cache?.bike, let new = result?.bike, old.id == new.id  {
            useButton.isHidden = false
            infoLabel.text = new.display
            return
        }
        cache = result
        guard multiTimer == nil else {
            useButton.isHidden = true
            infoLabel.text = "qr_error_multiple_text".localized()
            resetTimer()
            return
        }
        resetTimer()
        cache = nil
        infoLabel.text = result?.bike?.display
        useButton.isHidden = result?.bike == nil
    }
    
    private func resetTimer() {
        multiTimer?.invalidate()
        multiTimer = Timer.scheduledTimer(withTimeInterval: 3, repeats: false, block: { [weak self] (_) in
            self?.multiTimer?.invalidate()
            self?.multiTimer = nil
            if let result = self?.cache {
                self?.display(result: result)
            }
        })
    }
    
    @objc private func useAction() {
        useBlock()
    }
    
    @objc private func flash() {
        let flash = !flashButton.isSelected
        flashButton.isSelected = flash
        guard let device = AVCaptureDevice.default(for: AVMediaType.video)
            else {return}
        
        if device.hasTorch {
            do {
                try device.lockForConfiguration()
                
                if flash {
                    device.torchMode = .on
                } else {
                    device.torchMode = .off
                }
                
                device.unlockForConfiguration()
            } catch {
                print("Torch could not be used")
            }
        } else {
            print("Torch is not available")
        }
    }
}

extension QRCodeBike {
    var display: String {
        return String(format: "Name: %@\nID: %d", name, id)
    }
}
