//
//  SLSharingViewController.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 13/01/2017.
//  Copyright © 2017 Andre Green. All rights reserved.
//

import Foundation
import Localize_Swift
import Contacts
import PhoneNumberKit
import Crashlytics

final class SLSharingViewController: SLBaseViewController {
    fileprivate var fetchController = NSFetchedResultsController<SLLock>()
    fileprivate var contacts: [String: CNContact] = [:]
    fileprivate let contactHandler = SLContactHandler()
    fileprivate let tableView = UITableView()
    fileprivate let phoneNumberKit = PhoneNumberKit()
    fileprivate var selectedLock: SLLock?
    fileprivate var shouldShareLock = true
    fileprivate let locksService = LocksService()
    fileprivate let emptyLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 18)
        label.text = "You have no Ellipses\nto share".localized()
        label.textColor = .slWarmGrey
        label.textAlignment = .center
        label.numberOfLines = 0
        return label
    }()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        view.backgroundColor = .slWhite
        title = "SHARING".localized()
        addMenuButton()
        configureDataSource()
        configureTableView()
        updateContacts()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        navigationController?.isNavigationBarHidden = false
    }
    
    deinit {
        fetchController.delegate = nil
    }
    
    private func configureDataSource() {
        let dbMgr = SLDatabaseManager.shared()
        guard let user = dbMgr.getCurrentUser() else { return print("SLSharingViewController: cant get current user") }
        
        let request = NSFetchRequest<SLLock>(entityName: "SLLock")
        request.predicate = NSPredicate(format: "owner = %@", user)
        request.sortDescriptors = [NSSortDescriptor(key: "givenName", ascending: true)]
        
        fetchController = NSFetchedResultsController(fetchRequest: request, managedObjectContext: dbMgr.context, sectionNameKeyPath: nil, cacheName: nil)
        fetchController.delegate = self
        _ = try? fetchController.performFetch()
    }
    
    private func configureTableView() {
        view.addSubview(emptyLabel)
        emptyLabel.translatesAutoresizingMaskIntoConstraints = false
        emptyLabel.constrainEdges(to: view)
        view.addSubview(tableView)
        tableView.translatesAutoresizingMaskIntoConstraints = false
        tableView.constrainEdges(to: view)
        tableView.backgroundColor = .slWhite
        tableView.register(SLSharigTableViewCell.self, forCellReuseIdentifier: String(describing: SLSharigTableViewCell.self))
        tableView.register(SLSharigTableViewSharedCell.self, forCellReuseIdentifier: String(describing: SLSharigTableViewSharedCell.self))
        tableView.separatorStyle = .none
        tableView.delegate = self
        tableView.dataSource = self
        tableView.contentInset = UIEdgeInsets(top: 64, left: 0, bottom: 0, right: 0)
        reloadData()
    }
    
    private func updateContacts() {
        func process(contacts: [CNContact]) {
            for contact in contacts.filter({ $0.phoneNumber != nil }) {
                do {
                    let phone = try phoneNumberKit.parse(contact.phoneNumber!)
                    let number = phoneNumberKit.format(phone, toType: .e164)
                    self.contacts[number] = contact
                } catch (let error) {
                    print("Can't get phone for contact: \(contact)\nwith error: \(error)")
                }
            }
            DispatchQueue.main.async {
                self.tableView.reloadData()
            }
        }
        
        DispatchQueue.global().async {
            _ = try? self.contactHandler.allContacts { (contacts) in
                process(contacts: contacts)
            }
        }
    }
    
    fileprivate func openContacts(lockId: Int32) {
        let controller = SharingContactsViewController(lockId: lockId)
        navigationController?.pushViewController(controller, animated: true)
    }
    
    override func warningVCTakeActionButtonPressed(wvc: SLWarningViewController) {
        guard let lock = selectedLock, let userId = lock.borrower?.userId else { return }
        closeWarning { 
            self.presentLoadingViewWithMessage(message: "Unsharing lock...".localized())
            
            SLLockManager.sharedManager.revoke(lock: lock, unshareFrom: userId) { [weak self] (success) in
                self?.dismissLoadingViewWithCompletion(completion: nil)
                lock.borrower = nil
                SLDatabaseManager.shared().save(lock)
                self?.locksService.locks(updateCache: true)
                if let `self` = self, self.shouldShareLock {
                    self.openContacts(lockId: lock.lockId)
                }
                
                Answers.logShare(withMethod: "Unshare lock (by owner)", contentName: nil, contentType: nil, contentId: nil, customAttributes: nil)
            }
        }
    }
    
    fileprivate func reloadData() {
        self.tableView.reloadData()
        tableView.isHidden = fetchController.fetchedObjects?.isEmpty ?? false
    }
    
    fileprivate func presentUnshareWarning() {
        presentWarningViewControllerWithTexts(texts: [.Header : "CONFIRM ACTION".localized(),
                                                      .Info: "If you remove this Ellipse from your account, you will not be able to re-connect to it unless the owner grants you permission again.".localized(),
                                                      .ActionButton: "UNSHARE".localized(),
                                                      .CancelButton: "CANCEL".localized()], cancelClosure: nil)
    }
}

// MARK: - UITableViewDelegate, UITableViewDataSource
extension SLSharingViewController: UITableViewDelegate, UITableViewDataSource {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return fetchController.fetchedObjects?.count ?? 0
    }
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return fetchController.object(at: indexPath).rowHeight
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let lock = fetchController.object(at: indexPath)
        let cell = tableView.dequeueReusableCell(withIdentifier: lock.cellIdentifire, for: indexPath) as! SLSharigTableViewCell
        cell.delegate = self
        cell.lock = lock
        return cell
    }
}

// MARK: - NSFetchedResultsControllerDelegate
extension SLSharingViewController: NSFetchedResultsControllerDelegate {
    func controllerDidChangeContent(_ controller: NSFetchedResultsController<NSFetchRequestResult>) {
        DispatchQueue.main.async {
            self.reloadData()
        }
    }
}

// MARK: - SLSharigTableViewCellDelegate
extension SLSharingViewController: SLSharigTableViewCellDelegate {
    func share(lock: SLLock) {
        if lock.borrower != nil {
            selectedLock = lock
            shouldShareLock = true
            presentUnshareWarning()
            return
        }
        openContacts(lockId: lock.lockId)
    }
    
    func unshare(lock: SLLock) {
        selectedLock = lock
        shouldShareLock = false
        presentUnshareWarning()
    }
    
    func contact(forUser userId: String) -> CNContact? {
        return contacts[userId]
    }
}

private extension SLLock {
    var rowHeight: CGFloat {
        return borrower != nil ? 272 : 198
    }
    
    var cellIdentifire: String {
        return borrower != nil ? String(describing: SLSharigTableViewSharedCell.self) : String(describing: SLSharigTableViewCell.self)
    }
}
