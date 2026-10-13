# Remplit les bases avec un scenario de test complet :
# demande -> commande -> reception -> facture -> rapprochement -> paiement -> comptabilite.
# Relancer le script remet les donnees metier a zero. Les utilisateurs ne sont jamais supprimes.
# Usage (depuis infrastructure) : .\seed\seed.ps1

$ErrorActionPreference = "Stop"
$pg = docker compose -f "$PSScriptRoot\..\docker-compose.yml" ps -q postgres
if (-not $pg) { throw "Le conteneur postgres ne tourne pas : lancez d'abord docker compose up -d" }

function Invoke-Sql($db, $file, $vars = @()) {
    docker cp "$PSScriptRoot\$file" "${pg}:/tmp/$file" | Out-Null
    docker exec $pg psql -U postgres -d $db -q -v ON_ERROR_STOP=1 @vars -f "/tmp/$file"
    if ($LASTEXITCODE -ne 0) { throw "Echec du seed : $file" }
    docker exec $pg rm "/tmp/$file"
    Write-Host "OK  $db"
}

function Get-UserId($email) {
    $id = docker exec $pg psql -U postgres -d auth_db -t -A -c "select id from users where email = '$email'"
    if (-not $id) { throw "Utilisateur introuvable : $email (demarrez auth-service une fois pour creer tarik)" }
    return $id
}

Invoke-Sql auth_db auth.sql
$users = @("-v", "tarik_id=$(Get-UserId 'tarik@company.com')",
           "-v", "sara_id=$(Get-UserId 'sara@company.com')",
           "-v", "karim_id=$(Get-UserId 'karim@company.com')")

Invoke-Sql supplier_db supplier.sql
Invoke-Sql procurement_db procurement.sql $users
Invoke-Sql order_db order.sql
Invoke-Sql reception_db reception.sql
Invoke-Sql invoice_db invoice.sql
Invoke-Sql matching_db matching.sql
Invoke-Sql payment_db payment.sql
Invoke-Sql accounting_db accounting.sql
Invoke-Sql notification_db notification.sql $users

Write-Host ""
Write-Host "Seed termine. Comptes : tarik@company.com / tarik123 (ADMIN), karim@company.com / karim123 (MANAGER),"
Write-Host "sara@company.com / sara123 (EMPLOYEE), hamid@company.com / hamid123 (MAGASINIER), amina@company.com / amina123 (COMPTABLE)"
