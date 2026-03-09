using System;

namespace ServidorChat
{
    class Program
    {
        static void Main(string[] args)
        {
            Zerbitzaria servidor = new Zerbitzaria(5556); // puerto cambiado a 5556
            servidor.Iniciar();
        }
    }
}
