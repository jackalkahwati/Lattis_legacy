//
//  FindQRView.swift
//  Lattis
//
//  Created by Ravil Khusainov on 9/19/17.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit
import QRCodeReader
import SDWebImage
import AVFoundation

public struct QRCodeBike: Codable {
    public let name: String
    public let id: UInt
    
    public enum CodingKeys: String, CodingKey {
        case name = "bike_name"
        case id = "qr_id"
    }
}

public enum QRResult {
    case success(Bike)
    case fail(String)
    case multiple
}

protocol FindQRViewDelegate: class {
    func qrView(view: FindQRView, unlock bike: Bike)
    func qrView(viwe: FindQRView, show bike: Bike)
    func qrView(view: FindQRView, bike: QRCodeBike , getInfo: @escaping (QRResult) -> ()) -> Bool
    func addCreditCard(qrViwe: FindQRView)
}

class FindQROverlay: UIView, QRCodeReaderViewOverlay {
    func setState(_ state: QRCodeReaderViewOverlayState) {
        
    }
}

class FindQRView: UIView, QRCodeReaderDisplayable {
    func setNeedsUpdateOrientation() {
        
    }
    
    @IBOutlet weak var flashButton: UIButton!
    @IBOutlet weak var cardWarningView: UIView!
    @IBOutlet weak var batteryChargeView: UIView!
    @IBOutlet weak var textView: UITextView!
    @IBOutlet weak var alertCard: UIView!
    @IBOutlet weak var qrCard: UIView!
    @IBOutlet weak var bikeCard: UIView!
    @IBOutlet weak var alertTitleLabel: UILabel!
    @IBOutlet weak var alertSubtitleLabel: UILabel!
    @IBOutlet weak var loadingView: UIView!
    @IBOutlet weak var qrBikeLabel: UILabel!
    @IBOutlet weak var iconView: UIImageView!
    @IBOutlet weak var fleetLabel: UILabel!
    @IBOutlet weak var bikeLabel: UILabel!
    @IBOutlet weak var priceLabel: UILabel!
    @IBOutlet weak var durationLabel: UILabel!
    @IBOutlet weak var cardIconView: UIImageView!
    @IBOutlet weak var batteryLabel: UILabel!
    @IBOutlet weak var cardLabel: UILabel!
    @IBOutlet weak var batteryLevelView: UIView!
    @IBOutlet weak var batteryContainer: UIView!
    @IBOutlet weak var batteryRightConstraint: NSLayoutConstraint!
    @IBOutlet var camView: UIView!
    var cameraView: UIView {
        return camView
    }
    @IBOutlet var overView: FindQROverlay!
    var overlayView: QRCodeReaderViewOverlay? {
        return overView
    }
    @IBOutlet var canBtn: UIButton!
    var cancelButton: UIButton? {
        return canBtn
    }
    @IBOutlet var unlockButton: UIButton!
    let switchCameraButton: UIButton? = nil
    let toggleTorchButton: UIButton?  = nil
    
    func setupComponents(with builder: QRCodeReaderViewControllerBuilder) {
        cameraView.layer.insertSublayer(builder.reader.previewLayer, at: 0)
        flashButton.setImage(#imageLiteral(resourceName: "icon_flash_on"), for: .selected)
    }
    
    func setupComponents(showCancelButton: Bool, showSwitchCameraButton: Bool, showTorchButton: Bool, showOverlayView: Bool, reader: QRCodeReader?){
        if let reader = reader {
            cameraView.layer.insertSublayer(reader.previewLayer, at: 0)
        }
        flashButton.setImage(#imageLiteral(resourceName: "icon_flash_on"), for: .selected)
    }
    
    weak var delegate: FindQRViewDelegate?
    var bike: Bike? {
        didSet {
            unlockButton.isEnabled = bike != nil
            unlockButton.backgroundColor = bike != nil ? .lsTurquoiseBlue : .lsGrayishThree
            
            bikeLabel.text = bike?.name
            fleetLabel.text = bike?.fleetName
            if let logo = bike?.fleetLogo {
                iconView.sd_setImage(with: logo)
            } else {
                iconView.image = nil
            }
            if bike?.fleetType == .privatePay || bike?.fleetType == .publicPay, let price = bike?.priceForMembership, price > 0 {
                priceLabel.text = price.priceValue(bike!.currency)
            } else {
                priceLabel.text = "payment_cost_free".localized()
            }
            if bike?.fleetType == .privatePay || bike?.fleetType == .publicPay, let duration = bike?.priceDuration, let unit = bike?.priceUnit, duration > 0 {
                durationLabel.text = String(format: "payment_bike_card_per".localized(), "\(duration)", unit)
            } else {
                durationLabel.text = nil
            }
            batteryContainer.isHidden = true
            if let level = bike?.bikeBatteryLevel {
                let cf = CGFloat(1 - level)
                batteryRightConstraint.constant = cf*batteryChargeView.frame.width
                layoutIfNeeded()
                batteryLabel.text = bike?.bikeBatteryLevelString
            }
            if let terms = bike?.termsLink {
                let text = NSMutableAttributedString(string: "find_rite_terms_body_qr".localized(), attributes: [.font: UIFont.systemFont(ofSize: 9), .foregroundColor: textView.textColor!])
                let range = (text.string as NSString).range(of: "find_rite_terms_title".localized())
                text.addAttributes([.link: terms], range: range)
                textView.attributedText = text
                textView.linkTextAttributes = [.underlineStyle: NSUnderlineStyle.single.rawValue,
                                               .foregroundColor: textView.textColor!,
                                               .underlineColor: textView.textColor!]
            }
        }
    }
    
    fileprivate var qrId: UInt?
    
    @IBAction func unlockAction(_ sender: Any) {
        guard let bike = bike else { return }
        if (bike.fleetType == .privatePay || bike.fleetType == .publicPay) && CoreDataStack.shared.currentCard == nil {
            delegate?.addCreditCard(qrViwe: self)
            return
        }
        delegate?.qrView(view: self, unlock: bike)
    }
    
    @IBAction func infoAction(_ sender: Any) {
        guard let bike = bike else { return }
        delegate?.qrView(viwe: self, show: bike)
    }
    
    @IBAction func flashAction(_ sender: Any) {
        flashButton.isSelected = !flashButton.isSelected
        func toggleTorch(on: Bool) {
            guard let device = AVCaptureDevice.default(for: AVMediaType.video)
                else {return}
            
            if device.hasTorch {
                do {
                    try device.lockForConfiguration()
                    
                    if on == true {
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
        toggleTorch(on: flashButton.isSelected)
    }
    
    func display(result: QRCodeReaderResult?) {
        guard let qBike = result?.bike, qBike.id != qrId else { return }
        self.bike = nil
        qrId = qBike.id
        guard let del = delegate, del.qrView(view: self, bike: qBike, getInfo: { [weak self] res in
            switch res {
            case .fail(let error):
                self?.show(error: error)
            case .success(let bike):
                self?.show(bike: bike)
            case .multiple:
                self?.show(error: "qr_error_multiple_text".localized(), title: "qr_error_multiple_title".localized())
            }
        }) else { return }
        startLoading(bikeName: qBike.name)
    }
    
    func checkCreditCard() {
        guard let bike = bike else { return }
        if (bike.fleetType == .privatePay || bike.fleetType == .publicPay) && CoreDataStack.shared.currentCard != nil {
            unlockButton.setImage(#imageLiteral(resourceName: "icon_lock_locked"), for: .normal)
            unlockButton.setTitle(nil, for: .normal)
            cardWarningView.isHidden = true
        }
    }
}

private extension FindQRView {
    func startLoading(bikeName: String) {
        qrBikeLabel.text = bikeName
        overlayView?.bringSubviewToFront(qrCard)
        
        UIView.animate(withDuration: .defaultAnimation, animations: {
            self.alertCard.alpha = 0
            self.bikeCard.alpha = 0
            self.qrCard.alpha = 1
            self.loadingView.alpha = 1
        }, completion: nil)
        loadingView.frame = {
            var frame = qrCard.bounds
            frame.origin.x = -frame.width
            return frame
        }()
        UIView.animate(withDuration: 2, delay: 0, options: [.curveEaseInOut, .repeat], animations: { 
            self.loadingView.frame = {
                var frame = self.qrCard.bounds
                frame.origin.x = frame.width
                return frame
            }()
        }, completion: nil)
    }
    
    func show(bike: Bike) {
        guard bike.status == .active, bike.currentStatus == .parked else { return show(error: "qr_error_bike_not_live".localized()) }
        if (bike.fleetType == .privatePay || bike.fleetType == .publicPay) && CoreDataStack.shared.currentCard == nil {
            unlockButton.setImage(nil, for: .normal)
            unlockButton.setTitle("credit_card_no_current_action".localized(), for: .normal)
            cardWarningView.isHidden = false
        }
        self.bike = bike
        overlayView?.bringSubviewToFront(bikeCard)
        UIView.animate(withDuration: .defaultAnimation, animations: {
            self.alertCard.alpha = 0
            self.bikeCard.alpha = 1
            self.qrCard.alpha = 0
        }, completion: nil)
    }
    
    func show(error: String, title: String? = nil) {
        alertSubtitleLabel.text = error
        alertTitleLabel.text = title ?? qrBikeLabel.text
        overlayView?.bringSubviewToFront(alertCard)
        UIView.animate(withDuration: .defaultAnimation, animations: {
            self.alertCard.alpha = 1
            self.bikeCard.alpha = 0
            self.qrCard.alpha = 0
        }, completion: nil)
    }
}

extension QRCodeBike {
    var display: String {
        return String(format: "%@: %d", name, id)
    }
}

extension QRCodeReaderResult {
    var bike: QRCodeBike? {
        let decoder = JSONDecoder()
        guard let data = value.data(using: .utf8),
            let bike = try? decoder.decode(QRCodeBike.self, from: data) else { return nil }
        return bike
    }
}

