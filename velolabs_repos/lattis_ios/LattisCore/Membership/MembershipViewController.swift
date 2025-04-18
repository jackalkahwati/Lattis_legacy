//
//  MembershipViewController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 03.08.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import UIKit
import Cartography

class MembershipViewController: UIViewController {
    
    fileprivate let logic = MembershipLogicController()
    
    fileprivate let tableView = UITableView()

    override func viewDidLoad() {
        super.viewDidLoad()
        
        if #available(iOS 13.0, *) {
            overrideUserInterfaceStyle = .light
        }
        
        addCloseButton()
        
        view.backgroundColor = .white
        title = "memberships".localized()
        
        view.addSubview(tableView)
        
        constrain(tableView, view) { table, view in            
            table.top == view.safeAreaLayoutGuide.top + .margin
            table.left == view.left
            table.right == view.right
            table.bottom == view.safeAreaLayoutGuide.bottom - .margin
        }
        
        tableView.tableFooterView = UIView()
        tableView.separatorInset = .init(top: 0, left: .margin, bottom: 0, right: .margin)
        tableView.register(MembershipCell.self, forCellReuseIdentifier: "cell_membership")
        tableView.estimatedRowHeight = 66
        tableView.rowHeight = UITableView.automaticDimension
        tableView.sectionHeaderHeight = 48
        tableView.dataSource = self
        tableView.delegate = self
        
        logic.search = { [unowned self] in
            self.search()
        }
        logic.fetch { [weak self] (state) in
            self?.handle(state: state)
        }
    }
    
    fileprivate func handle(state: MembershipState) {
        switch state {
        case .failure(let error):
            handle(error)
        case .reload:
            tableView.reloadData()
        case .delete(let paths):
            tableView.beginUpdates()
            tableView.deleteRows(at: paths, with: .automatic)
            tableView.endUpdates()
        case .insert(let paths):
            tableView.beginUpdates()
            tableView.insertRows(at: paths, with: .automatic)
            tableView.endUpdates()
        }
    }
    
    fileprivate func search() {
        guard !logic.memberships.isEmpty else { return }
        let controller = MembershipSearchViewController(logic.memberships) { [unowned self] mem in
            self.navigationController?.pushViewController(MembershipDetailsViewController(mem), animated: true)
        }
        present(controller, animated: true)
    }
}

extension MembershipViewController: UITableViewDelegate, UITableViewDataSource {
    func numberOfSections(in tableView: UITableView) -> Int {
        logic.sections.count
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        logic.numberOfRows(in: section)
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let identifier = logic.sections[indexPath.section].cellIdentifier
        let cell = tableView.dequeueReusableCell(withIdentifier: identifier, for: indexPath)
        if let m = cell as? MembershipCell {
            if indexPath.section == 0 {
                m.subscription = logic.subscriptions[indexPath.row]
            } else {
                m.membership = logic.memberships[indexPath.row]
            }
        }
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        if indexPath.section == 0 {
            navigationController?.pushViewController(SubscriptionDetailsViewController(logic.subscriptions[indexPath.row]), animated: true)
        } else {
            navigationController?.pushViewController(MembershipDetailsViewController(logic.memberships[indexPath.row]), animated: true)
        }
    }
    
    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        MembershipsHeaderView(logic.actionForHeader(in: section))
    }
}
