//
//  RideHistoryDetailsRideHistoryDetailsViewController.swift
//  Lattis
//
//  Created by Ravil Khusainov on 18/08/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit

class RideHistoryDetailsViewController: ViewController {
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var fleetLabel: UILabel!
    @IBOutlet weak var dateLabel: UILabel!
    var interactor: RideHistoryDetailsInteractorInput!
    
    fileprivate let dateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .short
        return formatter
    }()
    fileprivate var sections: [Section] = []
    fileprivate var snapshot: UIImage?
    
    override var preferredStatusBarStyle: UIStatusBarStyle {
        return .lightContent
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        navigationItem.leftBarButtonItem = .back(target: self, action: #selector(close))
        title = "ride_details_title".localized()
        
        tableView.dataSource = self
        tableView.delegate = self
        tableView.rowHeight = UITableView.automaticDimension
        tableView.estimatedRowHeight = 100
        
        interactor.viewDidLoad()
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super .viewDidAppear(animated)
        
        interactor.requestSnapshot(size: CGSize(width: view.frame.width, height: 246))
    }
    
    @objc func close() {
        navigationController?.popViewController(animated: true)
    }
}

extension RideHistoryDetailsViewController: RideHistoryDetailsInteractorOutput {
    func present(snapshot: UIImage) {
        self.snapshot = snapshot
        tableView.beginUpdates()
        tableView.insertRows(at: [.init(row: 0, section: 1)], with: .automatic)
        tableView.endUpdates()
    }
    
    func show(trip: Trip, snapshot: UIImage?) {
        self.snapshot = snapshot
        let dateText = trip.finishedAt != nil ? dateFormatter.string(from: trip.finishedAt!) : nil
        dateLabel.text = defaultValue(dateText)
        fleetLabel.text = trip.fleetName
        sections.removeAll()
        if trip.fleetType == .privatePay || trip.fleetType == .publicPay {
            sections.append(.cost(trip))
        } else {
            sections.append(.free(trip))
        }
        sections.append(.summary(trip))
        tableView.reloadData()
    }
}

extension RideHistoryDetailsViewController: UITableViewDelegate, UITableViewDataSource {
    func numberOfSections(in tableView: UITableView) -> Int {
        return sections.count
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if snapshot != nil, case .summary = sections[section] {
            return 2
        }
        return 1
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let section = sections[indexPath.section]
        let cell = tableView.dequeueReusableCell(withIdentifier: section.identifire(snapshot != nil && indexPath.row == 0), for: indexPath) as! RideHistoryCell
        switch section {
        case .cost(let trip), .summary(let trip), .free(let trip):
            cell.trip = trip
        }
        if let c = cell as? RideHistoryMapCell {
            c.snapshotView.image = snapshot
        }
        return cell
    }
    
    func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        return 58
    }
    
    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        let view: RideHistoryDetailsHeader
        switch sections[section] {
        case .cost(let trip):
            view = RideHistoryDetailsCostHeader(card: trip.card)
        case .free:
            view = RideHistoryDetailsFreeHeader()
        default:
            view = RideHistoryDetailsHeader()
        }
        view.titleLabel.text = sections[section].title
        return view
    }
    
    func tableView(_ tableView: UITableView, heightForFooterInSection section: Int) -> CGFloat {
        let sec = sections[section]
        switch sec {
        case .cost(_):
            return 57
        default:
            return 0
        }
    }
    
    func tableView(_ tableView: UITableView, viewForFooterInSection section: Int) -> UIView? {
        let sec = sections[section]
        switch sec {
        case .cost(let trip):
            return RideHistoryFooter(trip: trip, shadow: sections.count > 1)
        default:
            return nil
        }
    }
}

extension RideHistoryDetailsViewController {
    enum Section {
        case cost(Trip)
        case summary(Trip)
        case free(Trip)
        
        func identifire(_ isSnapshot: Bool) -> String {
            switch self {
            case .free:
                return "free"
            case .cost:
                return "cost"
            default:
                if isSnapshot {
                    return "snapshot"
                }
                return "summary"
            }
        }
        
        var title: String {
            switch self {
            case .summary(_):
                return "ride_history_details_summary".localized()
            default:
                return "ride_history_details_cost".localized()
            }
        }
    }
}
