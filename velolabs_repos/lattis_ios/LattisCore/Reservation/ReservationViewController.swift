//
//  ScheduleViewController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 13.07.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import UIKit
import Cartography
import Model

class ReservationViewController: UIViewController {
    
    fileprivate let logic: ReservationLogicController
    fileprivate let stackView = UIStackView()
    fileprivate let actionView = ActionContainer(left: .ok)
    fileprivate let bikeActions = ActionContainer(left: .ok)
    fileprivate let countdownContainer = UIView()
    fileprivate let countdownLabel = UILabel.label(font: .theme(weight: .medium, size: .giant), color: .white, allignment: .center)
    
    init(reservation: Reservation) {
        logic = .init(reservation)
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

//        addCloseButton()
        title = "reservation".localized()
        view.backgroundColor = .white
        
        let scrollView = stackView.addScroll(insets: .init(top: 0, left: .margin, bottom: 0, right: .margin), to: view)
        
        view.addSubview(actionView)
        
        constrain(scrollView, actionView, view) { scroll, action, view in            
            action.bottom == view.safeAreaLayoutGuide.bottom - .margin
            action.left == view.left + .margin
            action.right == view.right - .margin
            
            scroll.top == view.safeAreaLayoutGuide.top
            scroll.left == view.left
            scroll.right == view.right
            scroll.bottom == action.top - .margin
        }
        
        stackView.axis = .vertical
        stackView.spacing = .margin/2
        
        let bikeItem = BikeItem(bike: logic.reservation.bike)
        bikeActions.update(left: .plain(title: nil, icon: .named("icon_map_bike"), style: .inactiveSecondary, handler: { [unowned self] in
            self.showBikeOnMap()
        }), right: .plain(title: nil, icon: .named("icon_info"), style: .inactiveSecondary, handler: { [unowned self] in
            self.openBikeDetails()
        }))
        
        countdownContainer.isHidden = true
        layoutCountdown()
        stackView.addArrangedSubview(countdownContainer)
        
        stackView.addArrangedSubview(bikeItem)
        stackView.addArrangedSubview(bikeActions)
        stackView.addArrangedSubview(.line)
        
        stackView.addArrangedSubview(
            UIStackView.tuple(
                UILabel.label(text: "pickup".localized(), font: .theme(weight: .medium, size: .small)),
                UILabel.label(text: logic.pickUpDate, font: .theme(weight: .bold, size: .text), allignment: .right)
            )
        )
        
        stackView.addArrangedSubview(
            UIStackView.tuple(
                UILabel.label(text: "return_label".localized(), font: .theme(weight: .medium, size: .small)),
                UILabel.label(text: logic.returnDate, font: .theme(weight: .bold, size: .text), allignment: .right)
            )
        )
        
        stackView.addArrangedSubview(
            UIStackView.tuple(
                UILabel.label(text: "price".localized(), font: .theme(weight: .medium, size: .small)),
                UILabel.label(text: logic.totalPrice, font: .theme(weight: .bold, size: .text), allignment: .right)
            )
        )
        
        if let parkingFee = logic.parkingFee {
            let tuple = UIStackView.tuple(
                UILabel.label(text: "bike_detail_label_parking_fee".localized(), font: .theme(weight: .medium, size: .small)),
                UILabel.label(text: parkingFee, font: .theme(weight: .bold, size: .text), allignment: .right)
            )
            stackView.addArrangedSubview(tuple)
            stackView.setCustomSpacing(0, after: tuple)
            
            stackView.addArrangedSubview(
                UILabel.label(text: "bike_detail_label_parking_fee_warning".localized(), font: .theme(weight: .medium, size: .small), color: .lightGray, lines: 0)
            )
        }
                
        let canStart = logic.countdown { [unowned self] (count) in
            self.countdownLabel.text = count
            if count == nil {
                self.actionView.update(
                    left: .plain(title: "cancel".localized(), style: .plain, handler: self.cancel),
                    right: .plain(title: "booking_begin_trip".localized(), handler: self.startTrip),
                    priority: .right
                )
                self.countdownContainer.isHidden = true
            }
        }
        if canStart {
            actionView.update(
                left: .plain(title: "cancel".localized(), style: .plain, handler: self.cancel),
                right: .plain(title: "booking_begin_trip".localized(), handler: self.startTrip),
                priority: .right
            )
        } else {
            actionView.update(
                left: .plain(title: "cancel".localized(), handler: cancel),
                right: nil
            )
        }
        
        logic.fetchCard { [weak self] (card) in
            self?.show(card: card)
        }
    }
    
    fileprivate func cancel() {
        let alert = AlertController(title: "general_error_title".localized(), message: .plain("reservation_cancel_warning".localized()))
        alert.actions = [
            .plain(title: "confirm".localized(), handler: performCancel),
            .cancel
        ]
        present(alert, animated: true, completion: nil)
    }
    
    fileprivate func performCancel() {
        startLoading("loading".localized())
        logic.cancel { [weak self] (e) in
            if let error = e {
                self?.handle(error)
            } else {
                self?.stopLoading {
                    self?.dismiss(animated: true, completion: nil)
                }
            }
        }
    }
    
    fileprivate func startTrip() {
        startLoading("starting_ride_loader".localized())
        logic.startTrip { [weak self] (result) in
            switch result {
            case .success(let trip):
                var tripToUpdate = trip
                let formatter = DateFormatter()
                formatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss"
                if let reservationEnd = self?.logic.reservation.reservationEnd {
                    tripToUpdate.reservationEnd = formatter.string(from: reservationEnd)
                }
                if let reservationStart = self?.logic.reservation.reservationStart {
                    tripToUpdate.reservationStart = formatter.string(from: reservationStart)
                }

                NotificationCenter.default.post(name: .tripStarted, object: tripToUpdate)
                self?.stopLoading(completion: {
                    self?.dismiss(animated: true, completion: nil)
                })
            case .failure(let error):
                self?.stopLoading {
                    self?.handle(error)
                }
            }
        }
    }
    
    fileprivate func openBikeDetails() {
        let details = BikeDetailsViewController_v2(logic.reservation.bike)
        details.closeButton.addTarget(self, action: #selector(close), for: .touchUpInside)
        present(details, animated: true)
    }
    
    fileprivate func showBikeOnMap() {
        let map = BikeLocationViewController.map(logic.reservation.bike)
        present(map, animated: true)
    }
    
    fileprivate func show(card: Payment.Card) {
        let paymentButton = UIButton(type: .custom)
        paymentButton.titleLabel?.font = .theme(weight: .bold, size: .text)
//        paymentButton.titleEdgeInsets = .init(top: 0, left: .margin/2, bottom: 0, right: 0)
        paymentButton.imageEdgeInsets = .init(top: 0, left: -.margin/2, bottom: 0, right: 0)
        paymentButton.contentHorizontalAlignment = .leading
        paymentButton.setImage(card.icon, for: .normal)
        paymentButton.setTitle(card.title, for: .normal)
        paymentButton.setTitleColor(.black, for: .normal)
        paymentButton.tintColor = .black
        
        stackView.addArrangedSubview(
            UIStackView.tuple(
                UILabel.label(text: "payment".localized(), font: .theme(weight: .medium, size: .small)),
                paymentButton
            )
        )
        stackView.addArrangedSubview(.line)
    }
    
    fileprivate func layoutCountdown() {
        let titleLabel = UILabel.label(text: "available_in".localized(), font: .theme(weight: .medium, size: .small))
        countdownContainer.addSubview(titleLabel)
        let bgView = UIView()
        bgView.backgroundColor = .accent
        bgView.layer.cornerRadius = .containerCornerRadius
        countdownContainer.addSubview(bgView)
        let iconView = UIImageView(image: .named("icon_duration"))
        bgView.addSubview(iconView)
        bgView.addSubview(countdownLabel)
        
        constrain(titleLabel, bgView, iconView, countdownLabel, countdownContainer) { title, bg, icon, count, container in
            title.top == container.top
            title.left == container.left
            title.right == container.right
            
            bg.top == title.bottom + .margin/4
            bg.left == container.left
            bg.right == container.right
            bg.bottom == container.bottom
            bg.height == 64
            
            icon.left == bg.left + .margin
            icon.centerY == bg.centerY
            
            count.centerY == bg.centerY
            count.centerX == bg.centerX ~ .defaultLow
            
            count.left >= icon.right + .margin/2
            count.right <= bg.right - .margin/2
        }
    }
}
