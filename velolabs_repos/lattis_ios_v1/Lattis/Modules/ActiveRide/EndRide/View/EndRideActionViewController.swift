//
//  EndRideActionViewController.swift
//  Lattis
//
//  Created by Ravil Khusainov on 28/03/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit
import Oval

class EndRideActionViewController: ViewController {
    @IBOutlet weak var ratingLabel: UILabel!
    @IBOutlet weak var ratingContainer: UIView!
    @IBOutlet weak var durationTitleLabel: UILabel!
    @IBOutlet weak var submitButton: UIButton!
    @IBOutlet weak var snapshotImageView: UIImageView!
    @IBOutlet weak var dateLabel: UILabel!
    @IBOutlet var rateButtons: [UIButton]!
    @IBOutlet weak var priceLabel: UILabel!
    @IBOutlet weak var durationLabel: UILabel!
    @IBOutlet weak var totalLabel: UILabel!
    @IBOutlet weak var durationRightConstraint: NSLayoutConstraint!
    var interactor: EndRideInteractorInput!
    
    fileprivate var rating: Int = 0
    
    override func viewDidLoad() {
        super.viewDidLoad()

        title = "trip_details_title".localized()
        navigationItem.leftBarButtonItem = .empty
        interactor.viewLoaded()
        rateButtons.forEach({ $0.setImage(#imageLiteral(resourceName: "icon_star_selected"), for: .selected) })
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        interactor.buildMap(size: snapshotImageView.frame.size)
    }

    @IBAction func submitAction(_ sender: UIButton) {
        rateButtons.forEach{$0.isSelected = $0.tag <= sender.tag}
        rating = sender.tag
    }
    
    @IBAction func finishAction(_ sender: Any) {
        self.interactor.submit(rating: rating)
    }
}

extension EndRideActionViewController: EndRideInteractorOutput {
    func show(snapshot: UIImage) {
        snapshotImageView.image = snapshot
    }

    func show(trip: Trip) {
        durationLabel.text = trip.duration.time
        if let total = trip.total, total > 0 {
            priceLabel.text = total.priceValue(trip.currency)
            totalLabel.isHidden = false
            durationLabel.textAlignment = .left
            durationTitleLabel.textAlignment = .left
            durationRightConstraint.priority = .init(rawValue: 750)
        } else {
            priceLabel.text = nil
            totalLabel.isHidden = true
            durationLabel.textAlignment = .center
            durationTitleLabel.textAlignment = .center
            durationRightConstraint.priority = .init(rawValue: 900)
        }
        
        if let date = trip.finishedAt {
            let formater = DateFormatter()
            formater.dateStyle = .long
            formater.timeStyle = .short
            dateLabel.text = formater.string(from: date).uppercased()
        }

            ratingLabel.isHidden = trip.isCanceled
        ratingContainer.isHidden = trip.isCanceled
        if trip.isCanceled {
            rating = 0
        }
    }
    
    override func show(error: Error, file: String, line: Int) {
        if let err = error as? SessionError, case .conflict = err.code {
            Analytics.report(error, file: file, line: line)
            let alert = ActionAlertView.alert(title: "end_ride_stripe_error_title".localized(), subtitle: "end_ride_stripe_error_text".localized())
            alert.action = AlertAction(title: "end_ride_stripe_error_action".localized(), action: interactor.openPayments)
            alert.cancel = AlertAction(title: "end_ride_stripe_error_cancel".localized(), action: interactor.dismiss)
            stopLoading {
                alert.show()
            }
        } else {
            super.show(error: error, file: file, line: line)
        }
    }
}
