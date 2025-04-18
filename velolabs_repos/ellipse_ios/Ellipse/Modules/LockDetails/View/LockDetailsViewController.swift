//
//  LockDetailsLockDetailsViewController.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 27/10/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import Cartography

class LockDetailsViewController: ViewController {
    var interactor: LockDetailsInteractorInput!
    var backToDashboard: Bool = false
    
    fileprivate let tableView = UITableView()
    fileprivate var alert: AlertView?
    fileprivate var progress: ProgressView?
    fileprivate let deleteButton = UIButton(type: .custom)

    override func viewDidLoad() {
        super.viewDidLoad()

        view.backgroundColor = .white
        title = "ellipse_settings".localized()
        addBackButton()
        configureUI()
        
        interactor.start()
    }
    
    fileprivate func configureUI() {
        navigationItem.largeTitleDisplayMode = .always
        view.addSubview(tableView)
        tableView.backgroundColor = .white
        constrain(tableView, view) { table, view in
            table.edges == view.edges
        }
        let footer = UIView(frame: .init(x: 0, y: 0, width: 0, height: 88))
        footer.backgroundColor = .white
        footer.addSubview(deleteButton)
        deleteButton.setTitle(interactor.isLockShared() ? "unshare_lock".localized() : "delete_lock".localized().lowercased().capitalized, for: .normal)
        deleteButton.addTarget(self, action: #selector(deleteLock(_:)), for: .touchUpInside)
        bigPositive(deleteButton)
        constrain(footer, deleteButton) { footer, delete in
            delete.center == footer.center
        }
        tableView.register(LockDetailsCell.self, forCellReuseIdentifier: "info")
        tableView.register(LockDetailsPinCell.self, forCellReuseIdentifier: "pin")
        tableView.register(LockDetailsFWCell.self, forCellReuseIdentifier: "fw")
        tableView.register(LockDetailsSensetivityCell.self, forCellReuseIdentifier: "sensetivity")
        tableView.separatorColor = .elPinkishGrey
        tableView.separatorInset = .init(top: 0, left: .margin, bottom: 0, right: .margin)
        tableView.sectionHeaderHeight = .rowHeight
        tableView.rowHeight = UITableView.automaticDimension
        tableView.estimatedRowHeight = 90
        tableView.sectionFooterHeight = 28
        tableView.tableFooterView = footer
        
        tableView.delegate = self
        tableView.dataSource = self
    }
    
    @objc fileprivate func deleteLock(_ sender: Any) {
        self.interactor.deleteLock()
    }
}

extension LockDetailsViewController: LockDetailsInteractorOutput {
    func beginFWUpdate() {
        let (alert, progress) = AlertView.update()
        self.alert = alert
        self.progress = progress
        stopLoading {
            alert.show()
        }
    }
    
    func updateFW(progress: Float) {
        self.progress?.progress = progress
    }
    
    func finishFWUpdate() {
        alert?.hide {
            self.startLoading(text: "Reconnecting...")
        }
    }
    
    func refresh() {
        tableView.reloadData()
    }
    
    func reloadRows(at indexPaths: [IndexPath]) {
        tableView.beginUpdates()
        tableView.reloadRows(at: indexPaths, with: .automatic)
        tableView.endUpdates()
    }
}

extension LockDetailsViewController: UITableViewDataSource, UITableViewDelegate {
    func numberOfSections(in tableView: UITableView) -> Int {
        return interactor.numberOfSections
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return interactor.numberOfRows(in: section)
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let info = interactor.item(for: indexPath)
        let cell = tableView.dequeueReusableCell(withIdentifier: info.identifire, for: indexPath) as! LockDetailsBaseCell
        cell.info = info
        if let update = cell as? LockDetailsFWCell {
            update.action = { [unowned self] in
                let alert = AlertView.alert(title: "firmware_update".localized(), text: self.interactor.changelog, actions: [
                    AlertView.Action(title: "install_update".localized().lowercased().capitalized, handler: { (_) in
                        self.interactor.update()
                    }),
                    AlertView.Action(title: "update_later".localized(), style: .cancel)
                ])
                alert.show()
            }
        }
        if let sensor = cell as? LockDetailsSensetivityCell {
            sensor.delegate = interactor
        }
        return cell
    }
    
    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        return TableSectionView(interactor.title(for: section))
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        interactor.select(itemAt: indexPath)
        tableView.deselectRow(at: indexPath, animated: true)
    }
}
