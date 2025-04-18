//
//  TripSummaryViewController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 07/08/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import Cartography

fileprivate extension UIButton {
    static func rating(_ rate: Int) -> UIButton {
        let button = UIButton(type: .custom)
        button.tag = rate
        button.setImage(.named("icon_rating_star"), for: .normal)
        button.tintColor = .lightGray
        return button
    }
}

class TripSummaryViewController: UIViewController {
    
    fileprivate let submitButton = ActionButton()
    fileprivate let ratingButtons: [UIButton] = [
        .rating(1),
        .rating(2),
        .rating(3),
        .rating(4),
        .rating(5)
    ]
    
    fileprivate let trip: Trip
    fileprivate let callback: () -> ()
    fileprivate let network: TripAPI = AppRouter.shared.api()
    fileprivate var rating: Int = 0
    fileprivate var mapImageView = UIImageView()
    
    init(_ trip: Trip, callback: @escaping () -> ()) {
        self.trip = trip
        self.callback = callback
        super.init(nibName: nil, bundle: nil)
        
        if #available(iOS 13.0, *) {
            isModalInPresentation = true
        }
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func viewDidLoad() {
        super.viewDidLoad()
        
        if #available(iOS 13.0, *) {
            overrideUserInterfaceStyle = .light
        }

        view.backgroundColor = .white
        
        let contentView = UIView()
        contentView.backgroundColor = .white
        
        let stackView = UIStackView()
        stackView.axis = .vertical
        stackView.spacing = .margin/2
        stackView.distribution = .fill
        contentView.addSubview(stackView)
        
        let durationView = UIView()
        durationView.backgroundColor = .accent
        durationView.layer.maskedCorners = [.layerMaxXMaxYCorner, .layerMaxXMinYCorner]
        durationView.layer.cornerRadius = 10
        contentView.addSubview(durationView)
        
        let timeFormatter = DateComponentsFormatter()
        if trip.duration > 60 {
            timeFormatter.allowedUnits = [.day, .hour, .minute]
        } else {
            timeFormatter.allowedUnits = [.hour, .minute, .second]
        }
        timeFormatter.unitsStyle = .short
        let durationStack = UIStackView(arrangedSubviews: [
            UILabel.label(
                text: "ride_summary_duration_label".localized(),
                font: .theme(weight: .light, size: .text),
                color: .white
            ),
            .circle(color: .white),
            UILabel.label(
                text: timeFormatter.string(from: trip.duration),
                font: .theme(weight: .bold, size: .text),
                color: .white
            )
        ])
        durationStack.axis = .horizontal
        durationStack.spacing = .margin/2
        durationStack.alignment = .center
        durationView.addSubview(durationStack)
        
        mapImageView.backgroundColor = .lightGray
        view.addSubview(mapImageView)
        view.addSubview(contentView)
        
        constrain(contentView, mapImageView, stackView, durationView, durationStack, view) { content, map, stack, duration, dStack, view in
            map.top == view.top
            map.left == view.left
            map.right == view.right
            map.bottom == content.top
            
            content.bottom == view.bottom - .margin
            content.left == view.left
            content.right == view.right
            
            duration.height == 38
            duration.bottom == content.top + 19
            duration.left == content.left
            
            stack.bottom == content.bottom - .margin
            stack.left == content.left + .margin
            stack.right == content.right - .margin
            stack.top == duration.bottom + .margin
            
            dStack.edges == duration.edges.inseted(horizontally: .margin/2)
        }
        
        submitButton.action = .plain(title: "submit".localized(), handler: { [unowned self] in
            self.submit()
        })
//        submitButton.setContentCompressionResistancePriority(.required, for: .horizontal)
        
        let ratingStack = UIStackView(arrangedSubviews: ratingButtons)
        ratingStack.axis = .horizontal
        ratingStack.spacing = .margin/2
        ratingStack.setContentCompressionResistancePriority(.defaultHigh, for: .horizontal)
        ratingButtons.forEach { button in
            button.addTarget(self, action: #selector(rate(_:)), for: .touchUpInside)
            button.setContentCompressionResistancePriority(.defaultHigh, for: .horizontal)
        }
        let ratingContainer = UIView()
        ratingContainer.addSubview(ratingStack)
        
        constrain(ratingContainer, ratingStack) { container, stack in
            stack.top == container.top
            stack.bottom == container.bottom
            stack.centerX == container.centerX
        }
        ratingContainer.setContentCompressionResistancePriority(.defaultHigh, for: .horizontal)
        
        let ratingLabel = UILabel.label(
            text: "rate_your_ride".localized(),
            font: .theme(weight: .book, size: .small),
            color: .gray,
            allignment: .center
        )
        
        if let end = trip.endedAt {
            let dateLabel = UILabel.label(
                text: DateFormatter.localizedString(from: end, dateStyle: .medium, timeStyle: .none),
                font: .theme(weight: .bold, size: .small),
                color: .black
            )
            stackView.addArrangedSubview(dateLabel)
        }
        
        stackView.addArrangedSubview(
            UILabel.label(
                text: "ride_summary_trip_summary_label".localized(),
                font: .theme(weight: .bold, size: .giant),
                color: .black
            )
        )
        
        stackView.addArrangedSubview(
            UIStackView.tuple(
                UILabel.label(text: "bike_detail_label_price".localized(), font: .theme(weight: .medium, size: .text)),
                UILabel.label(text: trip.price(for: .duration), font: .theme(weight: .bold, size: .text), allignment: .right)
            )
        )
        
        if let unlock = trip.price(for: .unlock) {
            stackView.addArrangedSubview(
                UIStackView.tuple(
                    UILabel.label(text: "unlock_fee".localized(), font: .theme(weight: .medium, size: .text)),
                    UILabel.label(text: unlock, font: .theme(weight: .bold, size: .text), allignment: .right)
                )
            )
        }
        
        if let surcharge = trip.price(for: .surcharge) {
            stackView.addArrangedSubview(
                UIStackView.tuple(
                    UILabel.label(text: "surcharge".localized(), font: .theme(weight: .medium, size: .text)),
                    UILabel.label(text: surcharge, font: .theme(weight: .bold, size: .text), allignment: .right)
                )
            )
        }
        
        if let penalty = trip.price(for: .penalty) {
            stackView.addArrangedSubview(
                UIStackView.tuple(
                    UILabel.label(text: "bike_detail_label_parking_fee".localized(), font: .theme(weight: .medium, size: .text)),
                    UILabel.label(text: penalty, font: .theme(weight: .bold, size: .text), allignment: .right)
                )
            )
        }
        
        if let amount = trip.discount, let text = trip.discountString(amount) {
            if amount > 0 {
                stackView.addArrangedSubview(
                    UIStackView.tuple(
                        UILabel.label(text: "membership".localized(), font: .theme(weight: .medium, size: .text)),
                        UILabel.label(text: text, font: .theme(weight: .bold, size: .text), allignment: .right)
                    )
                )
            }
        }

        if let amount = trip.promoCodeDiscount, let text = trip.discountString(amount) {
            stackView.addArrangedSubview(
                UIStackView.tuple(
                    UILabel.label(text: "promo_code".localized(), font: .theme(weight: .medium, size: .text)),
                    UILabel.label(text: text, font: .theme(weight: .bold, size: .text), allignment: .right)
                )
            )
        }
        
        if let taxes = trip.taxes {
            taxes.forEach { tax in
                let taxAmount = tax.amount.price(for: trip.currency)
                stackView.addArrangedSubview(
                    UIStackView.tuple(
                        UILabel.label(text: tax.name, font: .theme(weight: .medium, size: .text)),
                        UILabel.label(text: taxAmount, font: .theme(weight: .bold, size: .text), allignment: .right)
                    )
                )
            }
        }

        stackView.addArrangedSubview(.line)
        stackView.addArrangedSubview(
            UIStackView.tuple(
                UILabel.label(text: "ride_summary_total_label".localized(), font: .theme(weight: .bold, size: .body)),
                UILabel.label(text: trip.price(for: .total), font: .theme(weight: .bold, size: .body), allignment: .right)
            )
        )
        let linetView = UIView.line
        stackView.addArrangedSubview(linetView)
        stackView.setCustomSpacing(.margin, after: linetView)
        
        stackView.addArrangedSubview(ratingLabel)
        stackView.addArrangedSubview(ratingContainer)
        stackView.setCustomSpacing(.margin, after: ratingContainer)
        
        stackView.addArrangedSubview(submitButton)
//        ratingLabel.setContentHuggingPriority(.required, for: .vertical)
    }
    
    public override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        navigationController?.setNavigationBarHidden(true, animated: animated)
    }
    
    public override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        if mapImageView.image == nil {
            fetchMap()
        }
    }
    
    @objc fileprivate func submit() {
        callback()
        Analytics.log(.tripEnded(trip, rating: rating))
        network.rate(trip: trip.rate(rating)) { (result) in
            switch result {
            case .failure(let error):
                Analytics.report(error)
            default:
                break
            }
        }
    }
    
    @objc fileprivate func rate(_ sender: UIButton) {
        rating = sender.tag
        refreshButtons()
    }
    
    fileprivate func refreshButtons() {
        ratingButtons.forEach { (button) in
            button.tintColor = button.tag <= self.rating ? .accent : .lightGray
        }
    }
    
    fileprivate func fetchMap() {
        guard !trip.steps.isEmpty else { return mapImageView.stopPulse() }
        var size = mapImageView.frame.size
        if size == .zero {
            size = .init(width: 400, height: 200)
        }
        mapImageView.startPulse()
        network.fetchMap(start: trip.steps.first!.coordinate, finish: trip.steps.last!.coordinate, size: size) { [weak self] (result) in
            switch result {
            case .failure(let error):
                Analytics.report(error)
            case .success(let image):
                self?.update(map: image)
            }
        }
    }
    
    fileprivate func update(map: UIImage) {
        mapImageView.image = map
        mapImageView.stopPulse()
    }
}

