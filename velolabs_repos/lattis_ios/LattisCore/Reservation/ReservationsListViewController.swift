//
//  ReservationsListViewController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 12.08.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import UIKit
import Cartography
import Model

class ReservationsListViewController: UIViewController {
    
    fileprivate let createButton = ActionButton()
    fileprivate let tableView = UITableView()
    fileprivate let emptyLabel = UILabel.label(text: "reservations_empty_text".localized(), font: .theme(weight: .book, size: .body), lines: 0)
    
    fileprivate let logic: ReservationsListLogicController
    
    init(_ reservations: [Reservation]) {
        logic = .init(reservations)
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
        
        addCloseButton()

        view.backgroundColor = .white
        title = "reservations".localized()
        
        createButton.action = .plain(title: "create_a_reservation".localized()) { [unowned self] in self.createNewReservation() }
        view.addSubview(createButton)
        view.addSubview(tableView)
        view.addSubview(emptyLabel)
        emptyLabel.isHidden = !logic.reservations.isEmpty
        
        tableView.register(ReservationCell.self, forCellReuseIdentifier: "cell")
        tableView.delegate = self
        tableView.dataSource = self
        tableView.estimatedRowHeight = 88
        tableView.rowHeight = UITableView.automaticDimension
        tableView.tableFooterView = UIView()
        tableView.separatorInset = .init(top: 0, left: .margin, bottom: 0, right: .margin)
        
        constrain(createButton, tableView, emptyLabel, view) { create, table, empty, view in
            create.bottom == view.safeAreaLayoutGuide.bottom - .margin
            create.left == view.left + .margin
            create.right == view.right - .margin
            
            table.top == view.safeAreaLayoutGuide.top
            table.left == view.left
            table.right == view.right
            table.bottom == create.top - .margin
            
            empty.top == view.safeAreaLayoutGuide.top + .margin*2
            empty.left == view.left + .margin
            empty.right == view.right - .margin
        }
        
        logic.fetchReservations { [weak self] (state) in
            self?.render(state: state)
        }
    }
    
    fileprivate func createNewReservation() {
        let fleets = ReservationFleetsViewController()
        navigationController?.pushViewController(fleets, animated: true)
    }
    
    fileprivate func render(state: ReservationsState) {
        switch state {
        case .failure(let error):
            handle(error)
        case .update:
            emptyLabel.isHidden = !logic.reservations.isEmpty
            tableView.reloadData()
        }
    }
}

extension ReservationsListViewController: UITableViewDelegate, UITableViewDataSource {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        logic.reservations.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "cell", for: indexPath) as! ReservationCell
        cell.update(reservation: logic.reservations[indexPath.row], with: logic.dateFormatter, and: logic.timeFormatter)
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        let reservation = logic.reservations[indexPath.row]
        let schedule = ReservationViewController(reservation: reservation)
        navigationController?.pushViewController(schedule, animated: true)
    }
}
