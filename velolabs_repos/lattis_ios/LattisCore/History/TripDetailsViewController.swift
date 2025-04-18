//
//  TripDetailsViewController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 22/07/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import Cartography
import CoreLocation

class TripDetailsViewController: UIViewController {
    
    fileprivate let trip: Trip
    fileprivate let mapImageView = UIImageView()
    fileprivate let network: TripAPI = AppRouter.shared.api()
    
    init(_ trip: Trip) {
        self.trip = trip
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
        mapImageView.backgroundColor = .lightGray
        view.addSubview(mapImageView)
        
        let backButton = UIButton(type: .custom)
        backButton.setImage(.named("icon_back_arrow"), for: .normal)
        backButton.tintColor = .black
        backButton.addTarget(self, action: #selector(back), for: .touchUpInside)
        view.addSubview(backButton)
        
        let durationView = UIView()
        durationView.backgroundColor = .accent
        durationView.layer.maskedCorners  = [.layerMaxXMinYCorner, .layerMaxXMaxYCorner]
        durationView.layer.cornerRadius = 10
        view.addSubview(durationView)
        
        let dateLabel = UILabel.label(text: DateFormatter.localizedString(from: trip.startedAt, dateStyle: .medium, timeStyle: .medium), font: .theme(weight: .bold, size: .small))
        view.addSubview(dateLabel)
        
        let titleLabel = UILabel.label(text: "ride_summary_trip_summary_label".localized(), font: .theme(weight: .bold, size: .giant))
        view.addSubview(titleLabel)
        
        let scrollView = UIScrollView()
        view.addSubview(scrollView)
        
        let stackView = UIStackView()
        stackView.axis = .vertical
        stackView.spacing = .margin/2
        stackView.distribution = .fill
        scrollView.addSubview(stackView)
        
        constrain(backButton, mapImageView, durationView, dateLabel, titleLabel, scrollView, stackView, view) { back, map, duration, date, title, scroll, stack, view in
            back.left == view.left + .margin
            back.top == view.top + .margin/2
            
            map.top == view.top
            map.left == view.left
            map.right == view.right
            map.height == 280
            
            duration.centerY == map.bottom
            duration.left == view.left
            duration.height == 38
            
            date.top == duration.bottom + .margin
            date.left == view.left + .margin
            date.right == view.right - .margin
            
            title.top == date.bottom + .margin/4
            title.left == date.left
            title.right == date.right
            
            scroll.top == title.bottom + .margin
            scroll.left == title.left
            scroll.right == title.right
            scroll.bottom == view.safeAreaLayoutGuide.bottom
            
            stack.edges == scroll.edges
            stack.width == scroll.width
        }
        
        let durationTitle = UILabel.label(text: "ride_summary_duration_label".localized(), font: .theme(weight: .book, size: .text), color: .white)
        let durationFormatter = DateComponentsFormatter()
        if trip.duration < 60 {
            durationFormatter.allowedUnits = [.second]
        } else {
            durationFormatter.allowedUnits = [.day ,.hour, .minute]
        }
        durationFormatter.unitsStyle = .short
        let durationLabel = UILabel.label(text: durationFormatter.string(from: trip.duration), font: .theme(weight: .bold, size: .text), color: .white)
        let durationStack = UIStackView(arrangedSubviews: [durationTitle, .circle(color: .white), durationLabel])
        durationStack.axis = .horizontal
        durationStack.spacing = .margin/2
        durationStack.alignment = .center
        durationView.addSubview(durationStack)
        constrain(durationStack, durationView) { stack, view in
            stack.edges == view.edges.inseted(horizontally: .margin)
        }
        
        stackView.addArrangedSubview(addressView())
        stackView.addArrangedSubview(.line)
        priceItems().forEach{stackView.addArrangedSubview($0)}
        stackView.addArrangedSubview(.line)
        
        stackView.addArrangedSubview(
            UIStackView.tuple(
                UILabel.label(text: "ride_summary_total_label".localized(), font: .theme(weight: .bold, size: .body)),
                UILabel.label(text: trip.price(for: .total), font: .theme(weight: .bold, size: .title), allignment: .right)
            )
        )
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        navigationController?.setNavigationBarHidden(true, animated: animated)
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        navigationController?.setNavigationBarHidden(false, animated: animated)
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        if mapImageView.image == nil {
            fetchMap()
        }
    }
    
    fileprivate func fetchMap() {
        guard !trip.steps.isEmpty else { return }
        mapImageView.startPulse()
        let size = mapImageView.frame.size
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
    
    fileprivate func addressView() -> UIView {
        let container = UIView()
        let imageView = UIImageView(image: .named("icon_summary_address"))
        imageView.tintColor = .black
        container.addSubview(imageView)
        let startLabel = UILabel.label(text: trip.startAddress, font: .theme(weight: .book, size: .text))
        let endLabel = UILabel.label(text: trip.endAddress, font: .theme(weight: .book, size: .text))
        container.addSubview(startLabel)
        container.addSubview(endLabel)
        constrain(imageView, startLabel, endLabel, container) { image, start, end, view in
            image.left == view.left
            image.bottom == view.bottom - .margin/4
            image.top == view.top + .margin/4
            image.width == 11
            
            start.left == image.right + .margin/2
            start.top == view.top
            start.right == view.right
            
            end.left == start.left
            end.right == start.right
            end.bottom == view.bottom
        }
        return container
    }
    
    fileprivate func priceItems() -> [UIView] {
        var views = [UIView]()
        if let price = trip.price(for: .duration) {
            views.append(
                UIStackView.tuple(
                    UILabel.label(text: "metered_charges".localized(), font: .theme(weight: .medium, size: .text)),
                    UILabel.label(text: price, font: .theme(weight: .medium, size: .text), allignment: .right)
                )
            )
        }
        if let unlock = trip.price(for: .unlock) {
            views.append(
                UIStackView.tuple(
                    UILabel.label(text: "unlock_fee".localized(), font: .theme(weight: .medium, size: .text)),
                    UILabel.label(text: unlock, font: .theme(weight: .medium, size: .text), allignment: .right)
                )
            )
        }
        if let surcharge = trip.price(for: .surcharge) {
            views.append(
                UIStackView.tuple(
                    UILabel.label(text: "surcharge".localized(), font: .theme(weight: .medium, size: .text)),
                    UILabel.label(text: surcharge, font: .theme(weight: .medium, size: .text), allignment: .right)
                )
            )
        }
        if let parking = trip.price(for: .parking) {
            views.append(
                UIStackView.tuple(
                    UILabel.label(text: "bike_detail_label_parking_fee".localized(), font: .theme(weight: .medium, size: .text)),
                    UILabel.label(text: parking, font: .theme(weight: .medium, size: .text), allignment: .right)
                )
            )
        }
        if let discount = trip.discount{
            if discount > 0 {
                views.append(
                    UIStackView.tuple(
                        UILabel.label(text: "membership".localized(), font: .theme(weight: .medium, size: .text)),
                        UILabel.label(text: trip.discountString(discount), font: .theme(weight: .medium, size: .text), allignment: .right))
                )
            }
        }
        
        if let refund = trip.price(for: .refund) {
            views.append(
                UIStackView.tuple(
                    UILabel.label(text: "Refund", font: .theme(weight: .medium, size: .text)),
                    UILabel.label(text: refund, font: .theme(weight: .medium, size: .text), allignment: .right))
            )
        }

        if let amount = trip.promotion?.amount {
            views.append(
                UIStackView.tuple(
                    UILabel.label(text: "promo_code".localized(), font: .theme(weight: .medium, size: .text)),
                    UILabel.label(text: trip.discountString(amount), font: .theme(weight: .medium, size: .text), allignment: .right))
            )
        }
        
        if let taxes = trip.taxes {
            taxes.forEach { tax in
                let taxAmount = tax.amount.price(for: trip.currency)
                views.append(
                    UIStackView.tuple(
                        UILabel.label(text: tax.name, font: .theme(weight: .medium, size: .text)),
                        UILabel.label(text: taxAmount, font: .theme(weight: .medium, size: .text), allignment: .right))
                )
            }
        }

        return views
    }
}


