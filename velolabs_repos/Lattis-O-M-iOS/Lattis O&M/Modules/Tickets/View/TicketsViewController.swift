//
//  TicketsTicketsViewController.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 07/04/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

class TicketsViewController: ViewController {
    @IBOutlet weak var tableView: UITableView!
    var interactor: TicketsInteractorInput!

    fileprivate var tickets: [Ticket] = []
    fileprivate let refreshControl = UIRefreshControl()
    override func viewDidLoad() {
        super.viewDidLoad()
        
        tableView.dataSource = self
        tableView.delegate = self
        tableView.tableFooterView = UIView()
        
        interactor.viewLoaded()
        
        if #available(iOS 10.0, *) {
            tableView.refreshControl = refreshControl
        } else {
            tableView.addSubview(refreshControl)
        }
        refreshControl.addTarget(self, action: #selector(refresh), for: .valueChanged)
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        interactor.refreshTickets()
    }
    
    @objc fileprivate func refresh() {
        refreshControl.beginRefreshing()
        interactor.refreshTickets()
    }
    
    @IBAction func createTicket(_ sender: Any) {
        interactor.delegate.createTicket()
    }
}

extension TicketsViewController: TicketsInteractorOutput {
    func show(tickets: [Ticket]) {
        self.tickets = tickets
        tableView.reloadData()
        tableView.isHidden = tickets.count == 0
    }
    
    func endRefresh() {
        refreshControl.endRefreshing()
    }
}

extension TicketsViewController: UITableViewDataSource, UITableViewDelegate {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return tickets.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "ticket", for: indexPath) as! TicketCell
        cell.ticket = ticket(for: indexPath)
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        interactor.delegate.select(ticket: ticket(for: indexPath))
    }
}

private extension TicketsViewController {
    func ticket(for indexPath: IndexPath) -> Ticket {
        return tickets[indexPath.row]
    }
}
