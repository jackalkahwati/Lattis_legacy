//
//  ReservationViewController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 06.07.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import UIKit
import Cartography
import Atributika
import Model

fileprivate func reservationItemControl(_ content: UIView, title: String) -> UIControl {
    let control = UIControl()
    let disclosure = UIImageView(image: .named("icon_accessory_arrow"))
    let titleLabel = UILabel.label(text: title, font: .theme(weight: .medium, size: .small))
    disclosure.setContentHuggingPriority(.defaultHigh, for: .horizontal)
    control.addSubview(titleLabel)
    control.addSubview(disclosure)
    control.addSubview(content)
    
    titleLabel.isUserInteractionEnabled = false
    disclosure.isUserInteractionEnabled = false
    content.isUserInteractionEnabled = false
    
    constrain(titleLabel, disclosure, content, control) { title, disc, con, view in
        disc.centerY == con.centerY
        disc.right == view.right
        
        title.left == view.left
        title.right == disc.left - .margin/2
        title.top == view.top
        
        con.left == view.left
        con.right == title.right
        con.top == title.bottom + .margin/2
        con.bottom == view.bottom
    }
    
    return control
}

final class SelectionControl: UIControl {
    fileprivate let disclosure = UIImageView(image: .named("icon_accessory_arrow"))
    fileprivate let containerView = UIView()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        containerView.isUserInteractionEnabled = false
        addSubview(containerView)
        containerView.backgroundColor = .white
        containerView.layer.cornerRadius = .containerCornerRadius
        containerView.addShadow(offcet: .zero, radius: 5)
        addSubview(disclosure)
        disclosure.isUserInteractionEnabled = false
        disclosure.setContentHuggingPriority(.defaultHigh, for: .horizontal)
        
        constrain(containerView, disclosure, self) { container, arrow, view in
            arrow.right == view.right
            arrow.centerY == view.centerY

            container.right == arrow.left - .margin/2
            container.top == view.top
            container.bottom == view.bottom
            container.left == view.left
//            container.edges == view.edges.inseted(by: .margin)
        }
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func update(content: UIView) {
        containerView.subviews.forEach{$0.removeFromSuperview()}
        containerView.addSubview(content)
        content.isUserInteractionEnabled = false
        constrain(content, containerView) { $0.edges == $1.edges.inseted(by: .margin) }
        layoutIfNeeded()
    }
}

class NewReservationViewController: UIViewController {
    
    let logic: NewReservationLogicController

    fileprivate let continueButton = ActionButton()
    fileprivate let scrollView = UIScrollView()
    fileprivate var legalLabel: AttributedLabel!
    fileprivate let stackView = UIStackView()
    fileprivate let pickUpLabel = UILabel.label(text: "select_date_time".localized(), font: .theme(weight: .medium, size: .body))
    fileprivate let returnLabel = UILabel.label(text: "select_date_time".localized(), font: .theme(weight: .medium, size: .body))
    fileprivate let pricingLabel = UILabel.label(text: "select_pricing".localized(), font: .theme(weight: .medium, size: .body))
    fileprivate let paymentButton = UIButton(type: .custom)
    
    fileprivate var selectVehicleLabel: UILabel {
        UILabel.label(text: "see_available_vehicles".localized(), font: .theme(weight: .medium, size: .body))
    }
    
    init(fleet: Model.Fleet, settings: Reservation.Settings) {
        self.logic = .init(fleet: fleet, settings: settings)
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

        title = "reservation".localized()
        addCloseButton()
        
        stackView.axis = .vertical
        stackView.spacing = .margin/2
        
        view.backgroundColor = .white
        view.addSubview(scrollView)
        scrollView.addSubview(stackView)
        if let url = logic.legalUrl {
            legalLabel = .legal(self, text: "bike_details_terms_policy".localizedFormat(url))
        } else {
            legalLabel = .legal(self)
        }
        view.addSubview(legalLabel)
        view.addSubview(continueButton)
        
        constrain(continueButton, legalLabel, scrollView, stackView, view) { action, legal, scroll, stack, view in
            action.bottom == view.safeAreaLayoutGuide.bottom - .margin
            action.left == view.left + .margin
            action.right == view.right - .margin
            
            legal.left == action.left
            legal.right == action.right
            legal.bottom == action.top - .margin
            
            scroll.top == view.safeAreaLayoutGuide.top + .margin
            scroll.left == view.left
            scroll.right == view.right
            scroll.bottom == legal.top - .margin
            
            stack.edges == scroll.edges.inseted(horizontally: .margin)
            stack.width == scroll.width - .margin*2
        }
        
        let fleetTitle = UILabel.label(text: "fleet".localized(), font: .theme(weight: .medium, size: .small))
        stackView.addArrangedSubview(fleetTitle)
        
        let fleetIconView = UIImageView()
        let fleetNameLabel = UILabel.label(text: logic.fleet.name, font: .theme(weight: .medium, size: .body))
        let fleetView = UIView()
        fleetView.addSubview(fleetIconView)
        fleetView.addSubview(fleetNameLabel)
        constrain(fleetIconView, fleetNameLabel, fleetView) { icon, name, view in
            icon.left == view.left
            icon.bottom == view.bottom
            icon.top == view.top
            icon.height == 32
            icon.width == icon.height
            
            name.left == icon.right + .margin/2
            name.right == view.right
            name.centerY == view.centerY
        }
        stackView.addArrangedSubview(fleetView)
        stackView.addArrangedSubview(.line)
        fleetIconView.kf.setImage(with: logic.fleet.logo)
        
        let pickUp = reservationItemControl(pickUpLabel, title: "pickup".localized())
        pickUp.addTarget(self, action: #selector(selectPickUpDate), for: .touchUpInside)
        stackView.addArrangedSubview(pickUp)
        stackView.addArrangedSubview(.line)
        
        let ret = reservationItemControl(returnLabel, title: "return_label".localized())
        ret.addTarget(self, action: #selector(selectReturnDate), for: .touchUpInside)
        stackView.addArrangedSubview(ret)
        stackView.addArrangedSubview(.line)
        
        let vehicle = reservationItemControl(selectVehicleLabel, title: "vehicle".localized())
        vehicle.addTarget(self, action: #selector(selectVehicle), for: .touchUpInside)
        stackView.addArrangedSubview(vehicle)
        stackView.addArrangedSubview(.line)
        
        if logic.hasPricingOptions {
            let pricing = reservationItemControl(pricingLabel, title: "pricing".localized())
            pricing.addTarget(self, action: #selector(selectPricing), for: .touchUpInside)
            stackView.addArrangedSubview(pricing)
            stackView.addArrangedSubview(.line)
        }
                
        continueButton.action = .plain(title: "confirm".localized(), handler: { [unowned self] in
            self.confirm()
        })
        
        NotificationCenter.default.addObserver(self, selector: #selector(paymentMethodUpdated), name: .creditCardUpdated, object: nil)
    }
    
    fileprivate func confirm() {
        logic.confirm { [weak self] (state) in
            self?.render(state: state)
        }
    }
    
    fileprivate func cancel() {
        dismiss(animated: true, completion: nil)
    }
    
    @objc
    fileprivate func openPaymentMethods() {
        let payment = PaymentMethodsViewController(logic: .init(fleet: logic.fleet))
        navigationController?.pushViewController(payment, animated: true)
    }
    
    @objc
    fileprivate func selectVehicle() {
        logic.fetchBikes { [weak self] (state) in
            self?.render(state: state)
        }
    }
    
    @objc
    fileprivate func selectPickUpDate() {
        let picker = DatePickerController(title: "pickup".localized(), date: logic.startAt, min: logic.minDateStart, max: logic.maxDateStart) { [unowned self] (date) in
            self.logic.set(start: date) { [weak self] (state) in
                self?.render(state: state)
            }
        }
        present(picker, animated: true)
    }
    
    @objc
    fileprivate func selectReturnDate() {
        let picker = DatePickerController(title: "return_label".localized(), date: logic.endAt, min: logic.minDateEnd, max: logic.maxDateEnd) { [unowned self] (date) in
            self.logic.set(end: date) { [weak self] (state) in
                self?.render(state: state)
            }
        }
        present(picker, animated: true)
    }
    
    @objc
    fileprivate func selectPricing() {
        guard let options = logic.fleet.pricingOptions, !options.isEmpty else { return }
        let controller = PricingOptionsController(options, selected: logic.selectedPricing, perUseValue: logic.fleet.paymentSettings?.fullPrice)
        let vc = PricingOptionsViewController(controller) { [unowned self] selected in
            self.logic.set(pricing: selected) { [weak self] state in
                self?.render(state: state)
            }
            self.pricingLabel.text = logic.pricing
            self.navigationController?.popViewController(animated: true)
        }
        navigationController?.pushViewController(vc, animated: true)
    }
    
    fileprivate func select(from bikes: [Model.Bike]) {
        let controller = ReservationBikesViewController(bikes: bikes, selected: logic.bike) { [unowned self] bike in
            self.dismiss(animated: true)
            self.didSelect(bike: bike)
        }
        present(.navigation(controller), animated: true)
    }
    
    fileprivate func didSelect(bike: Model.Bike) {
        logic.set(bike: bike) { [unowned self] state in
            self.render(state: state)
        }
        
        let view = stackView.arrangedSubviews[7]
        stackView.removeArrangedSubview(view)
        view.removeFromSuperview()
        
        let control = BikeItem(bike: bike)
        let vehicle = reservationItemControl(control, title: "vehicle".localized())
        vehicle.addTarget(self, action: #selector(selectVehicle), for: .touchUpInside)
        stackView.insertArrangedSubview(vehicle, at: 7)
    }
    
    fileprivate func render(state: Reservation.State) {
        switch state {
        case .estimate(let price):
           handle(price: price)
        case .warning(let title, let message):
            warning(title: title, message: message, completion: nil)
        case .confirmation:
            let alert = AlertController(title: "confirmation".localized(), message: .plain("reservation_confirmed".localized()))
            alert.actions = [
                .plain(title: "ok".localized()) { [unowned self] in
                    self.performCancel()
                },
            ]
            stopLoading {
                self.present(alert, animated: true, completion: nil)
            }
//            DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
//                self.present(alert, animated: true, completion: nil)
//            }
        case .failure(let error):
            stopLoading {
                self.handle(error)
            }
        case .loading(let message):
            if let m = message {
                startLoading(m)
            } else {
                stopLoading()
            }
        case .cardRequred:
            let alert = AlertController.cardRequired(completion: openPaymentMethods)
            present(alert, animated: true, completion: nil)
        case .bikes(let bikes):
            stopLoading {
                self.select(from: bikes)
            }
        case .startDate(let dateString):
            pickUpLabel.text = dateString
        case .endDate(let dateString):
            returnLabel.text = dateString
        }
    }
    
    fileprivate func handle(price: Reservation.Price?) {
        if let view = stackView.arrangedSubviews.last as? UIStackView {
            stackView.removeArrangedSubview(view)
            view.removeFromSuperview()
        }
        guard let price = price else { return }
        let tripCosts = UILabel.label(text: "bike_detail_label_trip_costs".localized(), font: .theme(weight: .medium, size: .small))
        
        let priceView = UIView()
        priceView.backgroundColor = .secondaryBackground
        priceView.layer.cornerRadius = 5
        let contentView = UIStackView()
        priceView.addSubview(contentView)
        constrain(contentView, priceView) { content, view in
            content.edges == view.edges.inseted(by: .margin/2)
        }
        contentView.axis = .vertical
        contentView.spacing = .margin/2
        
        if let p = price.price {
            contentView.addArrangedSubview(
                UIStackView.tuple(
                    UILabel.label(text: "bike_detail_label_price".localized(), font: .theme(weight: .medium, size: .small)),
                    UILabel.label(text: p, font: .theme(weight: .bold, size: .text), allignment: .right)
                )
            )
        }
        if let p = price.parkingFee {
            let tuple = UIStackView.tuple(
                UILabel.label(text: "bike_detail_label_parking_fee".localized(), font: .theme(weight: .medium, size: .small)),
                UILabel.label(text: p, font: .theme(weight: .bold, size: .text), allignment: .right)
            )
            contentView.addArrangedSubview(tuple)
            contentView.setCustomSpacing(0, after: tuple)
            
            contentView.addArrangedSubview(
                UILabel.label(text: "bike_detail_label_parking_fee_warning".localized(), font: .theme(weight: .medium, size: .small), color: .lightGray, lines: 0)
            )
        }
        
        paymentButton.setTitleColor(.black, for: .normal)
        paymentButton.titleLabel?.font = .theme(weight: .bold, size: .text)
        paymentButton.contentHorizontalAlignment = .leading
        paymentButton.titleEdgeInsets = .init(top: 0, left: .margin/2, bottom: 0, right: 0)
        paymentButton.setImage(price.card?.icon, for: .normal)
        paymentButton.setTitle(price.card?.title, for: .normal)
        
        let payment = reservationItemControl(paymentButton, title: "payment".localized())
        payment.addTarget(self, action: #selector(openPaymentMethods), for: .touchUpInside)
        
        let priceStack = UIStackView(arrangedSubviews: [tripCosts, priceView, payment, .line])
        priceStack.axis = .vertical
        priceStack.spacing = .margin/2
        stackView.addArrangedSubview(priceStack)
    }

    fileprivate func performCancel() {
        stopLoading() {
            self.dismiss(animated: true, completion: nil)
        }
    }
    
    @objc
    fileprivate func paymentMethodUpdated(_ notification: Notification) {
        guard let payment = notification.object as? Payment else { return }
        paymentButton.setImage(payment.icon, for: .normal)
        paymentButton.setTitle(payment.title, for: .normal)
    }
}

extension PaymentMethodsLogicController {
    convenience init(fleet: Model.Fleet) {
        self.init(gateway: fleet.paymentSettings?.paymetnGateway ?? .stripe)
    }
}
