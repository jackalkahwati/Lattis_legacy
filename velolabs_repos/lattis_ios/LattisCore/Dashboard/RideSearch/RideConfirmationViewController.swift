//
//  RideConfirmationViewController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 06/06/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import Cartography
import Kingfisher
import SafariServices
import Atributika
import Model

class RideConfirmationViewController: UIViewController {
    
    let bikeControl: BikeControl
    fileprivate let contentView = UIStackView()
    let priceButton = DisclosureButton()
    
    var payPerUse: Bool = false
    fileprivate let storage = CardStorage()
    fileprivate let bike: Bike
    fileprivate var currentCard: Payment.Card?
    fileprivate var paymentIndex = 0
    fileprivate var paymentButton: UIButton?
    fileprivate let disconut: Double?
    fileprivate var pricing: Pricing?
    
    init(_ bike: Bike, disconut: Double?, pricing: Pricing?) {
        self.bike = bike
        self.disconut = disconut
        self.pricing = pricing
        self.bikeControl = .init(bike: bike)
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()

        if #available(iOS 13.0, *) {
            overrideUserInterfaceStyle = .light
        }
        view.backgroundColor = .white
        
        contentView.axis = .vertical
        contentView.spacing = .margin/2
        contentView.addArrangedSubview(bikeControl)
        
        contentView.addArrangedSubview(.line)
        
        let costsLabel = UILabel.label(text: "bike_detail_label_trip_costs".localized(), color: .gray)
        contentView.addArrangedSubview(costsLabel)
        
        let priceTupse = UIStackView.tuple(
            UILabel.label(text: "bike_detail_label_price".localized()),
            priceView
        )
        contentView.addArrangedSubview(priceTupse)
        
        if let unlock = bike.unlockPrice {
            let unlockTuple = UIStackView.tuple(
                UILabel.label(text: "unlock_fee".localized()),
                UILabel.label(text: unlock, font: .theme(weight: .bold, size: .text), allignment: .right)
            )
            contentView.addArrangedSubview(unlockTuple)
        }
        
        if let surcharge = bike.surchargePrice {
            let surchargeTuple = UIStackView.tuple(
                UILabel.label(text: "surcharge".localized()),
                UILabel.label(text: surcharge, font: .theme(weight: .bold, size: .text), allignment: .right)
            )
            contentView.addArrangedSubview(surchargeTuple)
            
            if let excess = bike.surchargeDescription {
                let excessLabel = UILabel.label(text: excess, font: .theme(weight: .book, size: .small), lines: 0)
                contentView.setCustomSpacing(3, after: surchargeTuple)
                contentView.addArrangedSubview(excessLabel)
            }
        }
        
        if !bike.isFree, let parking = bike.parkingPrice {
            let parking = UIStackView.tuple(
                UILabel.label(text: "bike_detail_label_parking_fee".localized()),
                UILabel.label(text: parking, font: .theme(weight: .bold, size: .text), allignment: .right)
            )
            contentView.addArrangedSubview(parking)
            
            let parkingFee = UILabel.label(text: "bike_detail_label_parking_fee_warning".localized(), font: .theme(weight: .book, size: .small), lines: 0)
            contentView.addArrangedSubview(parkingFee)
            contentView.setCustomSpacing(3, after: parking)
        }
        
        if let price = bike.preauthPrice {
            let preauth = UIStackView.tuple(
                UILabel.label(text: "preauthorization".localized()),
                UILabel.label(text: price, font: .theme(weight: .bold, size: .text), allignment: .right)
            )
            contentView.addArrangedSubview(preauth)
            
            let parkingFee = UILabel.label(text: "preauthorization_description".localized(), font: .theme(weight: .book, size: .small), lines: 0)
            contentView.addArrangedSubview(parkingFee)
            contentView.setCustomSpacing(3, after: preauth)
        }
        
        // has reservations
        if let reservation = bike.reservationRemaining {
            let reservedTime = UIStackView.tuple(
                UILabel.label(text: "rental_time_limit".localized()),
                UILabel.label(text: reservation,
                              font: .theme(weight: .bold, size: .text), allignment: .right)
            )
            contentView.addArrangedSubview(reservedTime)
            
            let reservedDesc = UILabel.label(text: bike.reservationDescription,
                                           font: .theme(weight: .book, size: .small), lines: 0)
            contentView.addArrangedSubview(reservedDesc)
            contentView.setCustomSpacing(3, after: reservedTime)
        }
        
        if let discount = disconut {
            if discount > 0 {
                contentView.addArrangedSubview(
                    UIStackView.tuple(
                        UILabel.label(text: "membership".localized()),
                        UILabel.label(text: "membership_discount_template".localizedFormat(discount.string()), font: .theme(weight: .bold, size: .text), allignment: .right))
                )
            }
        }

        if let promo = bike.promotions?.first {
            contentView.addArrangedSubview(
                UIStackView.tuple(
                    UILabel.label(text: "promo_code".localized()),
                    UILabel.label(text: "membership_discount_template".localizedFormat(promo.amount.string()), font: .theme(weight: .bold, size: .text), allignment: .right))
            )
        }

        paymentIndex = contentView.arrangedSubviews.count
        if let url = bike.terms {
            contentView.addArrangedSubview(.line)
            let legal = AttributedLabel.legal(self, text: "bike_details_terms_policy".localizedFormat(url))
            contentView.addArrangedSubview(legal)
        }
        handlePayment()
        view.addSubview(contentView)
        
        constrain(contentView, view) { stack, view in
            stack.edges == view.edges.inseted(horizontally: .margin)
        }
        
        NotificationCenter.default.addObserver(self, selector: #selector(handlePayment), name: .creditCardUpdated, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(handlePayment), name: .creditCardAdded, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(handlePayment), name: .creditCardRemoved, object: nil)
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
    
    fileprivate var priceView: UIView {
        let title = bike.price ?? "bike_detail_bike_cost_free".localized()
        if let options = bike.pricingOptions, !options.isEmpty {
            if payPerUse {
                priceButton.title = bike.fullPrice
            } else {
                priceButton.title = pricing?.title ?? "select_pricing".localized()
            }
            return priceButton
        } else {
            return UILabel.label(text: title, font: .theme(weight: .bold, size: .text), allignment: .right)
        }
    }
    
    @objc
    fileprivate func handlePayment() {
        guard !bike.isFree else { return }
        storage.fetch { [unowned self] (cards) in
            self.currentCard = cards.filter({$0.gateway == bike.paymentGateway}).first(where: {Payment.card($0).isCurrent})
            
            if let button = self.paymentButton {
                button.setTitle(self.currentCard?.title ??  Payment.addNew.title, for: .normal)
                button.setImage(self.currentCard?.icon ?? Payment.addNew.icon, for: .normal)
            } else {
                self.paymentButton = UIButton(type: .custom)
                self.paymentButton!.tintColor = .black
                self.paymentButton!.setTitleColor(.black, for: .normal)
                self.paymentButton!.setTitle(self.currentCard?.title ??  Payment.addNew.title, for: .normal)
                self.paymentButton!.setImage(self.currentCard?.icon ?? Payment.addNew.icon, for: .normal)
                self.paymentButton!.titleLabel?.font = .theme(weight: .bold, size: .text)
                self.paymentButton!.contentHorizontalAlignment = .leading
                self.paymentButton!.imageEdgeInsets = .init(top: 0, left: -.margin/2, bottom: 0, right: 0)
                self.paymentButton!.contentEdgeInsets = .init(top: 0, left: .margin/2, bottom: 0, right: 0)
                let paymentView = UIView()
                paymentView.addSubview(self.paymentButton!)
                let disclosureView = UIImageView(image: .named("icon_accessory_arrow"))
                disclosureView.setContentHuggingPriority(.defaultHigh, for: .horizontal)
                self.paymentButton?.setContentHuggingPriority(.defaultHigh, for: .horizontal)
                paymentView.addSubview(disclosureView)
                constrain(self.paymentButton!, disclosureView, paymentView) { button, disclosure, view in
                    view.height == 30
                    button.left == view.left
                    button.bottom == view.bottom
                    button.top == view.top
                    
                    disclosure.right == view.right
                    disclosure.centerY == view.centerY
                    button.right == disclosure.left - .margin/2
                }
                self.paymentButton!.addTarget(self, action: #selector(self.openPaymentMethods), for: .touchUpInside)
                let stack = UIStackView.tuple(UILabel.label(text: "payment".localized()), paymentView)
                self.contentView.insertArrangedSubview(stack, at: self.paymentIndex)
            }
            
        }
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        handlePayment()
    }
    
    @objc
    fileprivate func openPaymentMethods() {
        let payment = PaymentMethodsViewController(logic: .init(bike: bike))
        present(.navigation(payment), animated: true, completion: nil)
    }
    
    @objc
    fileprivate func openTermsAndConditions() {
        guard let terms = bike.terms, let url = URL(string: terms) else { return }
        let safari = SFSafariViewController(url: url)
        present(safari, animated: true, completion: nil)
    }
}

extension UILabel {
    static func label(text: String? = nil, font: UIFont = .theme(weight: .medium, size: .text), color: UIColor = .black, allignment: NSTextAlignment = .left, lines: Int = 1) -> UILabel {
        let label = UILabel()
        label.text = text
        label.textColor = color
        label.font = font
        label.numberOfLines = lines
        label.textAlignment = allignment
        return label
    }
}

extension UIStackView {
    static func tuple(_ lhs: UIView, _ rhs: UIView) -> UIStackView {
        let stack = UIStackView(arrangedSubviews: [lhs, rhs])
        stack.axis = .horizontal
        stack.spacing = .margin/2
        stack.distribution = .fill
        return stack
    }
}

extension UIControl {
    static func parrent(with subview: UIView) -> UIControl {
        let control = UIControl()
        control.addSubview(subview)
        subview.isUserInteractionEnabled = false
        constrain(subview, control) { $0.edges == $1.edges }
        return control
    }
}

extension UIView {
    static var line: UIView {
        let line = UIView()
        line.backgroundColor = .secondaryBackground
        constrain(line) { $0.height == 1 }
        return line
    }
    
    static func verticalLine(_ width: CGFloat = 1) -> UIView {
        let line = UIView()
        line.backgroundColor = .secondaryBackground
        constrain(line) { $0.width == width }
        return line
    }
}

extension PaymentMethodsLogicController {
    convenience init(bike: Bike) {
        self.init(gateway: bike.paymentGateway ?? .stripe)
    }
}
