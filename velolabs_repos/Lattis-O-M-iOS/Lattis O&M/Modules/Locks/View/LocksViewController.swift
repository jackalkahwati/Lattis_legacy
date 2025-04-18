//
//  LocksLocksViewController.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 07/04/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import LattisSDK
import Oval

class LocksViewController: ViewController {
    @IBOutlet weak var bluetoothView: UIView!
    @IBOutlet weak var emptyView: UIView!
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var filterButton: UIButton!
    var interactor: LocksInteractorInput!

    fileprivate let refreshControl = UIRefreshControl()
    fileprivate var macToConnect: String?
    fileprivate weak var filterView: LocksFilterSectionView?
    
    override func viewDidLoad() {
        super.viewDidLoad()

        tableView.tableFooterView = UIView()
        tableView.dataSource = self
        tableView.delegate = self
        tableView.refreshControl = refreshControl
        
        refreshControl.addTarget(self, action: #selector(refresh), for: .valueChanged)
        
        interactor.viewModel.change = { [weak self] refresh in
            self?.filterButton.setTitle(self?.interactor.viewModel.filter.title, for: .normal)
            self?.filterView?.subtitleLabel.text = String(format: "locks_filter_header_title".localized(), self!.interactor.viewModel.filter.title)
            self?.tableView.isHidden = refresh.isHidden
            if refresh.needsReload {
                self?.tableView.reloadData()
            } else if !refresh.isEmpty {
                self?.tableView.beginUpdates()
                if let sections = refresh.deleteSections {
                    self?.tableView.deleteSections(sections, with: .automatic)
                }
                if let move = refresh.moveSections {
                    self?.tableView.moveSection(move.from, toSection: move.to)
                }
                if let sections = refresh.insertSections {
                    self?.tableView.insertSections(sections, with: .automatic)
                }
                if let sections = refresh.reloadSetions {
                    self?.tableView.reloadSections(sections, with: .automatic)
                }
                if refresh.delete.isEmpty == false {
                    self?.tableView.deleteRows(at: refresh.delete, with: .automatic)
                }
                if refresh.update.isEmpty == false {
                    self?.tableView.reloadRows(at: refresh.update, with: .automatic)
                }
                if refresh.insert.isEmpty == false {
                    self?.tableView.insertRows(at: refresh.insert, with: .automatic)
                }
                self?.tableView.endUpdates()
            }
        }
        
        interactor.viewLoaded()
    }
    
    @objc fileprivate func refresh() {
        refreshControl.beginRefreshing()
        interactor.refresh()
        DispatchQueue.main.asyncAfter(deadline: .now() + 2) { [weak self] in
            self?.refreshControl.endRefreshing()
        }
    }
    
    @IBAction func addLock(_ sender: Any) {
        interactor.addLock()
    }
    
    @IBAction func changeFilter(_ sender: Any) {
        interactor.changeFilter()
    }
}

extension LocksViewController: LocksInteractorOutput {    
    func connectonFailed() {
        showAlert(title: "locks_qr_connection_timeout_title".localized(), subtitle: "locks_qr_connection_timeout_subtitle".localized())
        macToConnect = nil
    }
    
    func update(bluetoothState: Bool) {
        if bluetoothState == false {
            bluetoothView.alpha = 0
            bluetoothView.isHidden = false
            view.bringSubviewToFront(bluetoothView)
        }
        UIView.animate(withDuration: .defaultAnimation, animations: { 
            self.bluetoothView.alpha = bluetoothState ? 0 : 1
        }, completion: { _ in
            if bluetoothState {
                self.bluetoothView.isHidden = true
                self.view.sendSubviewToBack(self.bluetoothView)
            }
        })
    }
}

extension LocksViewController: UITableViewDataSource, UITableViewDelegate {
    func numberOfSections(in tableView: UITableView) -> Int {
        return interactor.viewModel.sectionsCount
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return interactor.viewModel.itemsCount(for: section)
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "lock", for: indexPath) as! LockCell
        cell.lock = interactor.viewModel.lock(for: indexPath)
//        cell.delegate = self
        return cell
    }
    
    func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        return interactor.viewModel.heightForHeader(in: section)
    }
    
    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        let view = interactor.viewForHeader(for: section)
        if let f = view as? LocksFilterSectionView {
            filterView = f
        }
        return view
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let lock = interactor.viewModel.lock(for: indexPath)
        let alert = AlertController(title: "Choose action", message: lock.displayTitle)
        if lock.lock == nil {
            alert.actions = [
                .plain(title: "locks_action_connect".localized(), handler: { [unowned self] in
                    self.startLoading()
                    self.interactor.viewModel.onboard(lock: lock) { [weak self] canUse in
                        if canUse {
                            self?.interactor.connect(lock: lock)
                        } else {
                            self?.showAlert(title: "You can't use that lock", subtitle: "It belongs to different fleet")
                        }
                    }
                }),
                .plain(title: "locks_action_blink".localized(), handler: { [unowned self] in
                    self.interactor.flashLED(for: lock)
                }),
                .cancel
            ]
            
        } else if lock.isConnected {
            alert.actions = [
                .plain(title: "Settings", handler: { [unowned self] in
                    self.interactor.delegate?.settings(lock: lock)
                }),
                .plain(title: "locks_action_dispatch".localized(), handler: { [unowned self] in
                    self.interactor.delegate?.dispatch(lock: lock)
                }),
                .plain(title: "locks_action_disconnect".localized(), handler: { [unowned self] in
                    self.interactor.disconnect(lock: lock)
                }),
                .cancel
            ]
        } else {
            alert.actions = [
                .plain(title: "locks_action_connect".localized(), handler: { [unowned self] in
                    self.interactor.connect(lock: lock)
                }),
                .plain(title: "locks_action_blink".localized(), handler: { [unowned self] in
                    self.interactor.flashLED(for: lock)
                }),
                .cancel
            ]
        }
        DispatchQueue.main.async {
            self.present(alert, animated: true, completion: nil)
        }
    }
}

extension LocksViewController {
    struct Section {
        let isConnected: Bool
        let title: String
        var items: [Lock]
        init(isConnected: Bool = false, title: String, items: [Lock]) {
            self.isConnected = isConnected
            self.title = title
            self.items = items
        }
    }
}

extension ErrorPresentable where Self: LocksViewController {
    func show(error: Error) {
        var title: String? = nil
        var subtitle: String? = nil
        if let error = error as? SessionError, case .unexpectedResponse = error.code {
            subtitle = "locks_qr_connection_failed_subtitle".localized()
            title = "locks_qr_connection_failed_title".localized()
        }
        if let error = error as? EllipseError {
            subtitle = "\(error)"
            showAlert(title: nil, subtitle: subtitle)
        }
        showAlert(title: title, subtitle: subtitle)
    }
}
