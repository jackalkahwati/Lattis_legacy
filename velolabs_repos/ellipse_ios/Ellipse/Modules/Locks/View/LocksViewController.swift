//
//  LocksLocksViewController.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 26/10/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import SwipeCellKit
import Cartography

class LocksViewController: ViewController {
    var interactor: LocksInteractorInput!
    
    fileprivate var emptyView: UIView?
    fileprivate let tableView = UITableView()

    override func viewDidLoad() {
        super.viewDidLoad()

        title = "ellipses".localized().lowercased().capitalized
        navigationItem.largeTitleDisplayMode = .always
        addMenuButton()
        navigationItem.rightBarButtonItem = UIBarButtonItem(image: UIImage(named: "button_add"), style: .plain, target: self, action: #selector(addNew(_:)))
        
        configureUI()
        
        interactor.start()
    }
    
    fileprivate func configureUI() {
        
        view.addSubview(tableView)
        tableView.backgroundColor = .white
        
        constrain(tableView, view) { table, view in
            table.edges == view.edges
        }
        
        tableView.register(LockCell.self, forCellReuseIdentifier: "ellipse")
        tableView.dataSource = self
        tableView.delegate = self
        tableView.tableFooterView = UIView()
        tableView.rowHeight = .rowHeightBig
        tableView.sectionHeaderHeight = .rowHeight
        tableView.separatorInset = .init(top: 0, left: .margin, bottom: 0, right: .margin)
    }
    
    @objc fileprivate func addNew(_ sender: Any) {
        interactor.addNew()
    }
}

extension LocksViewController: LocksInteractorOutput {
    func refresh() {
        tableView.reloadData()
    }
    
    func setEpty(hidden: Bool) {
        if hidden {
            emptyView?.removeFromSuperview()
        } else {
            let emptyView = UIView()
            let emptyLabel = UILabel()
            let addButton = UIButton(type: .custom)
            emptyView.backgroundColor = .white
            view.addSubview(emptyView)
            self.emptyView = emptyView
            emptyLabel.text = "locks_empty_text".localized()
            emptyLabel.textColor = .elSteel
            emptyLabel.font = .elTitle
            emptyLabel.textAlignment = .center
            emptyView.addSubview(emptyLabel)
            
            addButton.setTitle("add_new".localized(), for: .normal)
            smallPositiveStyle(addButton)
            emptyView.addSubview(addButton)
            
            addButton.addTarget(self, action: #selector(addNew(_:)), for: .touchUpInside)
            constrain(view, emptyView, emptyLabel, addButton) { view, empty, label, button  in
                empty.left == view.left + .margin
                empty.right == view.right - .margin
                empty.centerY == view.centerY
                
                label.left == empty.left
                label.right == empty.right
                label.top == empty.top
                
                button.centerX == empty.centerX
                button.bottom == empty.bottom
                button.top == label.bottom + .margin
            }
        }
    }
}

extension LocksViewController: UITableViewDataSource, UITableViewDelegate {
    func numberOfSections(in tableView: UITableView) -> Int {
        return interactor.numberOfsections
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return interactor.numberOfRows(in: section)
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "ellipse", for: indexPath) as! LockCell
        cell.lock = interactor.lock(for: indexPath)
        cell.delegate = self
        return cell
    }
    
    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        return TableSectionView(interactor.style(for: section))
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let cell = tableView.cellForRow(at: indexPath) as? LockCell
        let lock = interactor.lock(for: indexPath)
        if lock.isConnected {
            interactor.open(lock: lock)
        } else {
            cell?.showSwipe(orientation: .right)
        }
        tableView.deselectRow(at: indexPath, animated: true)
    }
}

extension LocksViewController: SwipeTableViewCellDelegate {
    func tableView(_ tableView: UITableView, editActionsForRowAt indexPath: IndexPath, for orientation: SwipeActionsOrientation) -> [SwipeAction]? {
        guard orientation == .right else { return [] }
        let lock = interactor.lock(for: indexPath)
        var actions: [SwipeAction] = []
        let font = UIFont.systemFont(ofSize: 11)
        let delete = SwipeAction(style: .destructive, title: lock.ellipse.isShared ? "unshare".localized().lowercased().capitalized : "delete".localized().lowercased().capitalized, handler: { [unowned self] (_, _) in
            AlertView.deleteLock(unshare: lock.ellipse.isShared) { [unowned self] in
                self.interactor.delete(lock: lock)
            }.show()
        })
        delete.image = UIImage(named: "trash_can")
        delete.font = font
        delete.backgroundColor = .elDarkSkyBlue
        delete.hidesWhenSelected = true
        actions.append(delete)
        if lock.peripheral != nil && !lock.isConnected {
            let connect = SwipeAction(style: .default, title: "connect".localized().lowercased().capitalized, handler: { [unowned self] (_, _) in
                self.interactor.connect(lock: lock)
            })
            connect.image = #imageLiteral(resourceName: "icon_connect")
            connect.font = font
            connect.backgroundColor = .elWindowsBlue
            connect.hidesWhenSelected = true
            actions.append(connect)
        }
        #if DEBUG
        if lock.isConnected {
            let disconnect = SwipeAction(style: .default, title: "Disconnect") { (_, _) in
                lock.peripheral?.disconnect()
                lock.ellipse.isCurrent = false
            }
            disconnect.font = font
            disconnect.backgroundColor = .elSteel
            disconnect.hidesWhenSelected = true
            actions.append(disconnect)
        }
        #endif
        return actions
    }
    
    func tableView(_ tableView: UITableView, didBeginEditingRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
    }
    
    func tableView(_ tableView: UITableView, didEndEditingRowAt indexPath: IndexPath?) {
        guard let index = indexPath else { return }
        tableView.deselectRow(at: index, animated: true)
    }
    
    func tableView(_ tableView: UITableView, didEndEditingRowAt indexPath: IndexPath?, for orientation: SwipeActionsOrientation) {
        guard let index = indexPath else { return }
        tableView.deselectRow(at: index, animated: true)
    }
}

extension TableSectionView {
    convenience init(_ style: Locks.Section.Style) {
        self.init(style.rawValue.localized())
        
        switch style {
        case .current:
            titleLabel.textColor = .elDarkSkyBlue
        case .previous, .unreachable:
            titleLabel.textColor = .black
        }
    }
}
