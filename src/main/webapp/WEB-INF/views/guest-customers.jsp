<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Guest Customers – Admin</title>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    <style>
        :root {
            --primary-500: #6366f1; --primary-600: #5b21b6; --primary-700: #4c1d95;
            --secondary-500: #10b981; --secondary-600: #059669; --secondary-700: #047857;
            --gray-50:#f9fafb; --gray-100:#f3f4f6; --gray-200:#e5e7eb; --gray-300:#d1d5db; --gray-600:#4b5563; --gray-900:#111827;
        }
        body { font-family: Inter, system-ui, -apple-system, sans-serif; background: var(--gray-50); color: var(--gray-900); }
        .dashboard { display:flex; min-height:100vh; }
        .sidebar { width: 280px; background: linear-gradient(180deg, var(--primary-600) 0%, var(--primary-700) 100%); color:#fff; position:fixed; height:100vh; left:0; top:0; box-shadow: 0 20px 25px -5px rgb(0 0 0 / .1); overflow-y: auto; }
        .sidebar .brand { display:flex; align-items:center; gap:1rem; padding:1.5rem 2rem; border-bottom:1px solid rgba(255,255,255,.1); }
        .brand-icon { width:48px; height:48px; border-radius:12px; background: rgba(255,255,255,.2); display:flex; align-items:center; justify-content:center; font-size:1.25rem; }
        .brand-text h1 { font-size:1.125rem; font-weight:800; margin:0; }
        .brand-text p { font-size:0.8125rem; opacity:0.85; margin:0; }
        .nav-menu { padding: 1rem 0; }
        .nav-group { margin-bottom: 1.5rem; }
        .nav-group-title { color: rgba(255,255,255,0.6); font-size: 0.75rem; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; padding: 0.5rem 2rem; margin-bottom: 0.5rem; }
        .nav-item { display:flex; align-items:center; gap:.75rem; color:rgba(255,255,255,0.9); text-decoration:none; padding:.85rem 2rem; border-radius:10px; margin:.25rem 1rem; transition:all 0.3s ease; }
        .nav-item i { width:20px; font-size:1rem; }
        .nav-item:hover { background: rgba(255,255,255,.15); color:#fff; transform: translateX(4px); }
        .nav-item.active { background: rgba(255,255,255,.2); color:#fff; font-weight:600; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
        .main { flex:1; margin-left:280px; display:flex; flex-direction:column; min-height:100vh; }
        .header { background:#fff; border-bottom:1px solid var(--gray-200); padding:1.25rem 2rem; display:flex; align-items:center; justify-content:space-between; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }
        .page-title { font-size:1.5rem; font-weight:800; color:var(--gray-900); display:flex; align-items:center; gap:0.75rem; }
        .page-title i { color:var(--primary-600); }
        .content { padding:2rem; }
        .grid { display:grid; grid-template-columns: 380px 1fr; gap:1.5rem; }
        .card { background:#fff; border:1px solid #e2e8f0; border-radius:14px; box-shadow: 0 10px 20px -10px rgb(2 6 23 / .12); overflow:hidden; }
        .card-header { padding:.9rem 1.25rem; border-bottom:1px solid #e2e8f0; font-weight:700; background:#fafafa; }
        .card-body { padding:1rem 1.25rem; }
        .list-scroll { max-height:70vh; overflow:auto; }
        .item { padding:.8rem .9rem; border:1px solid #e5e7eb; border-radius:12px; margin-bottom:.75rem; cursor:pointer; display:flex; justify-content:space-between; align-items:center; background:#fff; transition:.2s; }
        .item:hover { background:#f8fafc; transform: translateY(-1px); box-shadow: 0 6px 10px -8px rgba(2,6,23,.2); }
        .muted { color:#64748b; font-size:.85rem; }
        .btn { display:inline-flex; align-items:center; gap:.5rem; padding:.6rem .9rem; border-radius:10px; border:1px solid #cbd5e1; background:#fff; cursor:pointer; transition:.2s; }
        .btn:hover { transform: translateY(-1px); box-shadow: 0 8px 16px -12px rgba(2,6,23,.3); }
        .btn.danger { background:#fef2f2; border-color:#fecaca; color:#dc2626; }
        .table-wrap { border:1px solid #e2e8f0; border-radius:12px; overflow:hidden; box-shadow: 0 8px 16px -12px rgba(2,6,23,.2); }
        .table-scroll { max-height:60vh; overflow:auto; }
        table { width:100%; border-collapse:separate; border-spacing:0; }
        thead th { position:sticky; top:0; background:#f1f5f9; z-index:1; }
        th, td { padding:.7rem .9rem; border-bottom:1px solid #e5e7eb; text-align:left; font-size:.925rem; }
        tbody tr:hover { background:#f8fafc; }
        tbody tr:nth-child(odd) { background:#fcfcfd; }
        @media (max-width: 992px){ .main{margin-left:0;} .sidebar{display:none;} .grid{grid-template-columns:1fr;} .list-scroll{max-height:none;} }

        /* Delete Confirmation Modal */
        .delete-modal {display: none;position: fixed;top: 0;left: 0;width: 100%;height: 100%;background: rgba(0, 0, 0, 0.5);backdrop-filter: blur(4px);z-index: 9999;}
        .delete-modal.active {display: flex;align-items: center;justify-content: center;animation: fadeIn 0.3s ease;}
        .modal-content {background: white;border-radius: 20px;padding: 2.5rem;max-width: 480px;width: 90%;box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);animation: slideUp 0.3s ease;}
        @keyframes fadeIn {from { opacity: 0; }to { opacity: 1; }}
        @keyframes slideUp {from { transform: translateY(30px); opacity: 0; }to { transform: translateY(0); opacity: 1; }}
        .modal-icon {width: 64px;height: 64px;margin: 0 auto 1.5rem;background: linear-gradient(135deg, #fee2e2, #fecaca);border-radius: 50%;display: flex;align-items: center;justify-content: center;font-size: 2rem;color: #dc2626;}
        .modal-title {font-size: 1.5rem;font-weight: 700;margin-bottom: 0.75rem;text-align: center;}
        .modal-message {color: #4b5563;text-align: center;margin-bottom: 2rem;line-height: 1.6;}
        .modal-actions {display: flex;gap: 1rem;}
        .modal-btn {flex: 1;padding: 0.875rem 1.5rem;border: none;border-radius: 12px;font-weight: 600;font-size: 1rem;cursor: pointer;transition: all 0.3s ease;}
        .modal-btn-cancel {background: white;color: #4b5563;border: 2px solid #d1d5db;}
        .modal-btn-cancel:hover {background: #f3f4f6;}
        .modal-btn-delete {background: linear-gradient(135deg, #ef4444, #dc2626);color: white;box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);}
        .modal-btn-delete:hover {transform: translateY(-2px);box-shadow: 0 8px 20px rgba(239, 68, 68, 0.4);}
        
        /* Success Modal */
        .success-modal .modal-icon {
            background: linear-gradient(135deg, #d1fae5, #a7f3d0);
            color: #059669;
        }
        .success-modal .modal-title {
            color: #065f46;
        }
        .modal-btn-ok {
            flex: 1;
            padding: 0.875rem 1.5rem;
            border: none;
            border-radius: 12px;
            font-weight: 600;
            font-size: 1rem;
            cursor: pointer;
            background: linear-gradient(135deg, #10b981, #059669);
            color: white;
            box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
            transition: all 0.3s ease;
        }
        .modal-btn-ok:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 20px rgba(16, 185, 129, 0.4);
        }
    </style>
    <script>
        let currentUserId = null;
        let currentName = null;

        function viewGuest(userId){ window.location.href = '<c:url value="/admin/guests"/>?userId=' + encodeURIComponent(userId); }
        
        function deleteGuest(userId, name){ 
            currentUserId = userId;
            currentName = name;
            document.getElementById('deleteGuestName').textContent = name;
            document.getElementById('deleteGuestId').textContent = userId;
            document.getElementById('deleteModal').classList.add('active');
            document.body.style.overflow = 'hidden';
        }

        function closeDeleteModal() {
            document.getElementById('deleteModal').classList.remove('active');
            document.body.style.overflow = 'auto';
            currentUserId = null;
            currentName = null;
        }

        function confirmDelete() {
            if (!currentUserId) return;
            const f=document.getElementById('guestForm'); 
            f.action = '<c:url value="/admin/guest/"/>' + encodeURIComponent(currentUserId) + '/delete'; 
            f.submit();
        }

        // Close on outside click
        document.addEventListener('click', function(e) {
            if (e.target.classList.contains('delete-modal')) {
                closeDeleteModal();
            }
        });

        // Close on ESC key
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape') {
                closeDeleteModal();
            }
        });

        // Show success modal if message exists
        document.addEventListener('DOMContentLoaded', function() {
            const successMsg = document.getElementById('successMessage');
            if (successMsg) {
                showSuccessModal(successMsg.textContent);
            }
        });

        function showSuccessModal(message) {
            document.getElementById('successModalMessage').textContent = message;
            document.getElementById('successModal').classList.add('active');
            document.body.style.overflow = 'hidden';
        }

        function closeSuccessModal() {
            document.getElementById('successModal').classList.remove('active');
            document.body.style.overflow = 'auto';
        }
    </script>
    <form id="guestForm" method="post" style="display:none;"></form>

    <!-- Delete Confirmation Modal -->
    <div id="deleteModal" class="delete-modal">
        <div class="modal-content">
            <div class="modal-icon">
                <i class="fas fa-exclamation-triangle"></i>
            </div>
            <h2 class="modal-title">Delete Guest?</h2>
            <p class="modal-message">
                Are you sure you want to delete guest <strong id="deleteGuestName"></strong> (<span id="deleteGuestId"></span>)? 
                <br><br>
                <span style="color: #dc2626; font-weight: 600;">This action cannot be undone!</span>
            </p>
            <div class="modal-actions">
                <button type="button" class="modal-btn modal-btn-cancel" onclick="closeDeleteModal()">
                    <i class="fas fa-times"></i> Cancel
                </button>
                <button type="button" class="modal-btn modal-btn-delete" onclick="confirmDelete()">
                    <i class="fas fa-trash"></i> Delete Guest
                </button>
            </div>
        </div>
    </div>

    <!-- Success Modal -->
    <div id="successModal" class="delete-modal success-modal">
        <div class="modal-content">
            <div class="modal-icon">
                <i class="fas fa-check-circle"></i>
            </div>
            <h2 class="modal-title">Success!</h2>
            <p class="modal-message" id="successModalMessage"></p>
            <div class="modal-actions">
                <button type="button" class="modal-btn-ok" onclick="closeSuccessModal()">
                    <i class="fas fa-check"></i> OK
                </button>
            </div>
        </div>
    </div>
</head>
<body>
<div class="dashboard">
    <aside class="sidebar">
        <div class="brand">
            <div class="brand-icon"><i class="fas fa-shield-alt"></i></div>
            <div class="brand-text">
                <h1>PharmaCare</h1>
                <p>Admin Panel</p>
            </div>
        </div>
        <nav class="nav-menu">
            <div class="nav-group">
                <div class="nav-group-title">Overview</div>
                <a href="<c:url value='/admin/dashboard'/>" class="nav-item">
                    <i class="fas fa-tachometer-alt"></i>Dashboard
                </a>
            </div>

            <div class="nav-group">
                <div class="nav-group-title">User Management</div>
                <a href="<c:url value='/admin/users'/>" class="nav-item">
                    <i class="fas fa-users"></i>All Users
                </a>
                <a href="<c:url value='/admin/add-user'/>" class="nav-item">
                    <i class="fas fa-user-plus"></i>Add User
                </a>
            </div>

            <div class="nav-group">
                <div class="nav-group-title">Customer Management</div>
                <a href="<c:url value='/admin/loyalty-members'/>" class="nav-item">
                    <i class="fas fa-crown"></i>Loyalty Members
                </a>
                <a href="<c:url value='/admin/guests'/>" class="nav-item active">
                    <i class="fas fa-user"></i>Guest Customers
                </a>
            </div>

            <div class="nav-group">
                <div class="nav-group-title">Business Operations</div>
                <a href="<c:url value='/inventory/dashboard'/>" class="nav-item">
                    <i class="fas fa-boxes"></i>Inventory
                </a>
                <a href="<c:url value='/admin/bills'/>" class="nav-item">
                    <i class="fas fa-receipt"></i>Bill Management
                </a>
            </div>

            <div class="nav-group">
                <div class="nav-group-title">Account</div>
                <a href="<c:url value='/logout'/>" class="nav-item">
                    <i class="fas fa-sign-out-alt"></i>Logout
                </a>
            </div>
        </nav>
    </aside>
    <main class="main">
        <header class="header">
            <div class="page-title"><i class="fas fa-user"></i> Guest Customers</div>
            <a class="btn" href="<c:url value='/admin/dashboard'/>"><i class="fas fa-arrow-left"></i> Back</a>
        </header>
        <div class="content">
            <c:if test="${not empty success}">
                <div id="successMessage" style="display:none;">${success}</div>
            </c:if>
            <c:if test="${not empty error}">
                <div style="background:#fef2f2; border:1px solid #fecaca; color:#991b1b; padding:.8rem 1rem; border-radius:10px; margin-bottom:1rem;">
                    <i class="fas fa-exclamation-circle"></i> ${error}
                </div>
            </c:if>
            <div class="grid">
                <div class="card">
                    <div class="card-header">Guests</div>
                    <div class="card-body list-scroll">
                        <c:if test="${empty guests}"><div class="muted">No guests found.</div></c:if>
                        <c:forEach var="g" items="${guests}">
                            <div class="item" onclick="viewGuest('${g.userId}')">
                                <div>
                                    <div><strong>${g.name}</strong></div>
                                    <div class="muted">${g.userId} • ${g.phoneNumber}</div>
                                </div>
                                <button type="button" class="btn danger" onclick="event.stopPropagation(); deleteGuest('${g.userId}', '${g.name}')"><i class="fas fa-trash"></i> Delete</button>
                            </div>
                        </c:forEach>
                    </div>
                </div>
                <div class="card">
                    <div class="card-header">Guest Details</div>
                    <div class="card-body">
                        <c:choose>
                            <c:when test="${not empty selectedGuest}">
                                <div style="display:flex; align-items:center; justify-content:space-between; margin-bottom:.75rem;">
                                    <div>
                                        <div><strong>${selectedGuest.name}</strong></div>
                                        <div class="muted">${selectedGuest.userId} • ${selectedGuest.phoneNumber}</div>
                                    </div>
                                    <button type="button" class="btn danger" onclick="deleteGuest('${selectedGuest.userId}', '${selectedGuest.name}')"><i class="fas fa-user-times"></i> Delete Guest</button>
                                </div>
                                <h4 style="margin:1rem 0 .5rem 0; font-size:1.05rem; font-weight:800;">Payment History</h4>
                                <c:if test="${empty payments}"><div class="muted">No payments found.</div></c:if>
                                <c:if test="${not empty payments}">
                                    <div class="table-wrap"><div class="table-scroll">
                                        <table>
                                            <thead>
                                            <tr>
                                                <th>Date</th>
                                                <th>Method</th>
                                                <th>Total</th>
                                                <th>Points Used</th>
                                                <th>Points Earned</th>
                                                <th>Status</th>
                                            </tr>
                                            </thead>
                                            <tbody>
                                            <c:forEach var="p" items="${payments}">
                                                <tr>
                                                    <td><c:out value="${p.paymentDate}"/></td>
                                                    <td>${p.paymentMethod}</td>
                                                    <td>Rs. <c:out value="${p.total}"/></td>
                                                    <td><c:out value="${p.pointsUsed}"/></td>
                                                    <td><c:out value="${p.pointsEarned}"/></td>
                                                    <td>${p.paymentStatus}</td>
                                                </tr>
                                            </c:forEach>
                                            </tbody>
                                        </table>
                                    </div></div>
                                </c:if>
                            </c:when>
                            <c:otherwise>
                                <div class="muted">Select a guest to view details and payment history.</div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </div>
    </main>
</div>
</body>
</html>


