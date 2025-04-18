//
//  ScanToStartViewController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 25.07.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import UIKit
import Cartography
import AVFoundation
import QRCodeView

class ScanToStartViewController: UIViewController {
    
    fileprivate let bike: Bike
    fileprivate let completion: () -> ()
    fileprivate let titleLabel = UILabel.label(text: "scan_qr_code_title".localized(), font: .theme(weight: .medium, size: .giant))
    fileprivate let closeButton = UIButton(type: .custom)
    fileprivate let subtitleLabel = UILabel.label(font: .theme(weight: .book, size: .title))
    fileprivate let torchButton = UIButton(type: .custom)
    fileprivate let readerView = QRCodeView()
    fileprivate let urlDecoder: QRCodeView.Decoder = .url()
    fileprivate let bikeDecoder: QRCodeView.Decoder<Bike.QRCode> = .json()
    fileprivate let focusView = UIView()
    
    init(_ bike: Bike, completion: @escaping () -> ()) {
        self.bike = bike
        self.completion = completion
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()

        if #available(iOS 13.0, *) {
            overrideUserInterfaceStyle = .light
        }
        view.backgroundColor = .white
        
        subtitleLabel.text = "find_qr_code_on".localizedFormat(bike.name)
        
        closeButton.setImage(.named("icon_close"), for: .normal)
        closeButton.addTarget(self, action: #selector(close), for: .touchUpInside)
        closeButton.tintColor = .black
        view.addSubview(titleLabel)
        view.addSubview(closeButton)
        
        readerView.layer.cornerRadius = .containerCornerRadius
        torchButton.layer.cornerRadius = 24
        torchButton.backgroundColor = .secondaryBackground
        torchButton.setImage(.named("icon_torch"), for: .normal)
        torchButton.tintColor = .lightGray
        torchButton.addTarget(self, action: #selector(handleTorch), for: .touchUpInside)
        subtitleLabel.numberOfLines = 0
        
        view.addSubview(subtitleLabel)
        view.addSubview(readerView)
        view.addSubview(torchButton)
        
        constrain(titleLabel, closeButton, subtitleLabel, readerView, torchButton, view) { title, close, subtitle, scanner, torch, view in
            title.top == view.safeAreaLayoutGuide.top + .margin
            title.left == view.left + .margin
            title.right == close.left - .margin/2
            
            close.right == view.right - .margin
            close.centerY == title.centerY
            
            subtitle.top == title.bottom + .margin/2
            subtitle.left == view.left + .margin
            subtitle.right == view.right - .margin
            
            scanner.left == subtitle.left
            scanner.right == subtitle.right
            scanner.top == subtitle.bottom + .margin
            scanner.bottom == torch.top - .margin
            
            torch.left == subtitle.left
            torch.right == subtitle.right
            torch.bottom == view.safeAreaLayoutGuide.bottom - .margin/2
            torch.height == 80
        }

        readerView.addSubview(focusView)
        focusView.alpha = 1
        focusView.isUserInteractionEnabled = false
        
        let topLeftView = UIImageView(image: .named("icon_scanner_corner"))
        let topRightView = UIImageView(image: .named("icon_scanner_corner"))
        topRightView.transform = topRightView.transform.rotated(by: .pi/2)
        let bottomLeftView = UIImageView(image: .named("icon_scanner_corner"))
        bottomLeftView.transform = bottomLeftView.transform.rotated(by: -.pi/2)
        let bottomRightView = UIImageView(image: .named("icon_scanner_corner"))
        bottomRightView.transform = bottomRightView.transform.rotated(by: .pi)
        focusView.addSubview(topLeftView)
        focusView.addSubview(topRightView)
        focusView.addSubview(bottomRightView)
        focusView.addSubview(bottomLeftView)
        
        constrain(topLeftView, topRightView, bottomLeftView, bottomRightView, focusView, readerView) { topLeft, topRight, bottomLeft, bottomRight, view, reader in
            topLeft.left == view.left
            topLeft.top == view.top
            
            topRight.right == view.right
            topRight.top == view.top
            
            bottomLeft.left == view.left
            bottomLeft.bottom == view.bottom
            
            bottomRight.right == view.right
            bottomRight.bottom == view.bottom
            
            view.edges == reader.edges.inseted(by: .margin)
        }
        
        readerView.found = { [unowned self] in self.handle($0) }
        readerView.startScan()
        #if targetEnvironment(simulator)
        DispatchQueue.main.asyncAfter(deadline: .now() + 2, execute: completion)
        #endif
    }
    
    fileprivate func handle(_ code: String) -> Bool {
//        readerView.stopScan()
//        completion()
//        return true
//        guard let bikeQr = bike.qrCode else { return false }
        var bikeQr = code
        if  bike.qrCode != nil {
            bikeQr = bike.qrCode!
        }

        func report() {
            Analytics.log(.qrCodeScanned(vehicle: bike.bikeId))
        }
        if let url = urlDecoder.decode(code),
            url.scheme != nil,
            bikeQr == url.lastPathComponent {
            readerView.stopScan()
            completion()
            report()
            return true
        }
        if let bike = bikeDecoder.decode(code),
            bikeQr == String(bike.qr_id) {
            readerView.stopScan()
            completion()
            report()
            return true
        }
        let match = bikeQr == code
        if match {
            readerView.stopScan()
            completion()
            report()
        }
        return match
    }

    @objc
    fileprivate func handleTorch() {
        guard let device = AVCaptureDevice.default(for: AVMediaType.video),
            device.hasTorch else {
                print("Torch is not available")
                return
        }
        do {
            try device.lockForConfiguration()
            
            if device.torchMode != .on {
                device.torchMode = .on
                torchButton.tintColor = .black
            } else {
                device.torchMode = .off
                torchButton.tintColor = .lightGray
            }
            
            device.unlockForConfiguration()
        } catch {
            print("Torch could not be used")
        }
    }
}
